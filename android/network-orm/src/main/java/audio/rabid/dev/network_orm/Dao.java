package audio.rabid.dev.network_orm;

import android.os.AsyncTask;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by charles on 10/23/15.
 */
public class Dao<T extends Resource> extends RuntimeExceptionDao<T, Integer> {

    public Dao(com.j256.ormlite.dao.Dao<T, Integer> dao) {
        super(dao);
    }

    public void findByLocalId(final int id, SingleQueryCallback<T> callback){
        (new SingleItemQuery<T>(callback){
            @Override
            protected T doInBackground(Void... params) {
                return queryForId(id);
            }
        }).execute();
    }

    public void findByRemoteId(final int serverId, SingleQueryCallback<T> callback){
        findBy("serverId", serverId, callback);
    }

    public void findBy(final String field, final Object value, SingleQueryCallback<T> callback){
        (new SingleItemQuery<T>(callback){
            @Override
            protected T doInBackground(Void... params) {
                List<T> results = queryForEq(field, value);
                return results.size() > 0 ? results.get(0) : null;
            }
        }).execute();
    }

    public void all(MultipleQueryCallback<T> callback){
        (new MultipleItemQuery<T>(callback){
            @Override
            protected List<T> doInBackground(Void... params) {
                return queryForAll();
            }
        }).execute();
    }

    public void unSynced(MultipleQueryCallback<T> callback){
        (new MultipleItemQuery<T>(callback){
            @Override
            protected List<T> doInBackground(Void... params) {
                return queryForEq("synced", false);
            }
        }).execute();
    }

    public void save(final T object, @Nullable SingleQueryCallback<T> callback){
        (new SingleItemQuery<T>(callback){
            @Override
            protected T doInBackground(Void... params) {
                createOrUpdate(object);
                return object;
            }
        }).execute();
    }

    public void delete(final T object, @Nullable SingleQueryCallback<T> callback){
        (new SingleItemQuery<T>(callback){
            @Override
            protected T doInBackground(Void... params) {
                delete(object);
                return object;
            }
        }).execute();
    }

    /***************************************************************/

    private static abstract class SingleItemQuery<Q extends Resource> extends AsyncTask<Void, Void, Q> {

        private SingleQueryCallback<Q> callback;
        public SingleItemQuery(@Nullable SingleQueryCallback<Q> callback){
            this.callback = callback;
        }

        @Override
        protected void onPostExecute(Q result){
            if(callback!=null) callback.onResult(result);
        }
    }

    public interface SingleQueryCallback<Q> {
        void onResult(Q result);
    }

    private static abstract class MultipleItemQuery<Q extends Resource> extends AsyncTask<Void, Void, List<Q>> {

        private MultipleQueryCallback<Q> callback;
        public MultipleItemQuery(@Nullable MultipleQueryCallback<Q> callback){
            this.callback = callback;
        }

        @Override
        protected void onPostExecute(List<Q> results){
            if(callback!=null) callback.onResult(results);
        }
    }

    public interface MultipleQueryCallback<Q> {
        void onResult(List<Q> results);
    }
}
