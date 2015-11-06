package audio.rabid.dev.sampleapp.models;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import audio.rabid.dev.roe.models.IntegerKeyedNetworkResource;
import audio.rabid.dev.roe.models.IntegerKeyedResource;
import audio.rabid.dev.roe.models.JSONField;
import audio.rabid.dev.roe.models.NetworkResource;
import audio.rabid.dev.roe.models.Source;
import audio.rabid.dev.roe.models.Op;
import audio.rabid.dev.roe.models.rails.RailsResource;
import audio.rabid.dev.roe.models.rails.RailsSource;
import audio.rabid.dev.sampleapp.Database;
import audio.rabid.dev.sampleapp.R;
import audio.rabid.dev.sampleapp.SampleAppServer;
import audio.rabid.dev.utils.ImageCache;

/**
 * Created by charles on 10/23/15.
 */
@DatabaseTable(tableName = "authors")
@RailsResource(endpoint = "authors", singularJSONKey = "author", pluralJSONKey = "authors")
public class Author extends IntegerKeyedNetworkResource<Author> {

    public static final RailsSource<Author, Integer> Source = new RailsSource.Builder<>
            (SampleAppServer.getInstance(), Database.getInstance(), Author.class)
            .setPermissions(Op.CREATE, Op.READ, Op.UPDATE).build();

    //TODO
//    static {
//        try {
//            final Dao<Author, ?> adao = Database.getInstance().getDao(Author.class);
//            adao.callBatchTasks(new Callable<Author>() {
//                @Override
//                public Author call() throws Exception {
//                    for (Author a : adao.queryForAll()) {
//                        a.synced = false;
//                        a.serverId = null;
//                        adao.update(a);
//                    }
//                    return null;
//                }
//            });
//        }catch (Exception e){
//            throw new RuntimeException(e);
//        }
//    }

    @JSONField
    @DatabaseField
    private String name;

    @JSONField
    @DatabaseField
    private String email;

    @JSONField
    @DatabaseField
    private String avatar;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public URL getAvatar() {
        if (avatar == null) return null;
        try {
            return new URL(avatar);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private Bitmap avatarBitmap;

    public void getAvatarBitmap(final BitmapCallback callback) {
        //no image saved
        if (avatar == null) {
            callback.onBitmapReady(null);
            return;
        }

        //image cached locally
        if (avatarBitmap != null) {
            callback.onBitmapReady(avatarBitmap);
            return;
        }
        URL url = getAvatar();
        //invalid url
        if (url == null) {
            callback.onBitmapReady(null);
            return;
        }
        //fetch from network
        (new AsyncTask<URL, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(URL... params) {
                Bitmap b = ImageCache.getInstance().get(avatar);
                if (b != null) {
                    return b;
                } else {
                    try {
                        HttpURLConnection connection = (HttpURLConnection) params[0].openConnection();
                        return BitmapFactory.decodeStream(connection.getInputStream());
                    } catch (IOException e) {
                        return null;
                    }
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                avatarBitmap = bitmap;
                if (bitmap != null && avatar != null) {
                    ImageCache.getInstance().put(avatar, bitmap);
                }
                callback.onBitmapReady(bitmap);
            }
        }).execute(url);
    }

    public interface BitmapCallback {
        void onBitmapReady(@Nullable Bitmap bitmap);
    }

    public void setAvatar(String avatar) {
        try {
            new URL(avatar);
            this.avatar = avatar;
            //clear saved images
            ImageCache.getInstance().remove(avatar);
            avatarBitmap = null;
        } catch (MalformedURLException e) {
            //oops
        }
    }

    public void sendEmail(Context context) {
        if (email == null) return;
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getEmail(), null));
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{getEmail()});
        context.startActivity(Intent.createChooser(i, context.getString(R.string.contact_author)));
    }

    @Override
    public Source<Author, Integer> getSource() {
        return Source;
    }
}
