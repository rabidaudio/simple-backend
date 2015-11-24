package audio.rabid.dev.sampleapp.models;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

import audio.rabid.dev.roe.models.NetworkSyncableDao;
import audio.rabid.dev.roe.models.json.JSONField;
import audio.rabid.dev.roe.models.rails.RailsServerResource;
import audio.rabid.dev.roe.models.resource.IntegerKeyedResource;
import audio.rabid.dev.sampleapp.Database;
import audio.rabid.dev.sampleapp.R;
import audio.rabid.dev.utils.ImageCache;

/**
 * Created by charles on 10/23/15.
 */
@DatabaseTable(tableName = "authors", daoClass = NetworkSyncableDao.class)
@RailsServerResource(endpoint = "/authors")
public class Author extends IntegerKeyedResource {

    public static NetworkSyncableDao<Author, Integer, Integer> AuthorDao = Database.getInstance().getDao(Author.class);

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

    public Future<Author> save(@Nullable NetworkSyncableDao.OperationCallback<Author> callback){
        return AuthorDao.saveAsync(this, callback);
    }
}
