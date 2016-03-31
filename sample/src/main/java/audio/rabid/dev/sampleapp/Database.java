package audio.rabid.dev.sampleapp;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.jdeferred.android.AndroidDeferredManager;

import audio.rabid.dev.roe.models.RoeManager;
import audio.rabid.dev.roe.models.cache.WeakMapResourceCache;
import audio.rabid.dev.sampleapp.models.Author;
import audio.rabid.dev.sampleapp.models.AuthorModel;
import audio.rabid.dev.sampleapp.models.Post;
import audio.rabid.dev.sampleapp.models.PostModel;

/**
 * Created by charles on 10/23/15.
 */
public class Database extends RoeManager {

    private static Database instance;

    public static Database getInstance() {
        if (instance == null)
            throw new RuntimeException("Need to create an instance with context first");
        return instance;
    }

    public static Database createInstance(Context context) {
        if (instance == null) {
            try {
                instance = new Database(context);
            }catch (SnappydbException e){
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    private DB database;

    private Database(Context context) throws SnappydbException {
        super(new AndroidDeferredManager(), new WeakMapResourceCache());
        database = DBFactory.open(context);

        addModel(Author.class, new AuthorModel(database));
        addModel(Post.class, new PostModel(database));
    }

    @Override
    protected void finalize() throws Throwable {
        database.close();
        super.finalize();
    }
}
