package audio.rabid.dev.network_orm;

import com.j256.ormlite.field.DatabaseField;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;

/**
 * Created by charles on 10/23/15.
 */
public abstract class Resource<T extends Resource> extends Observable {

    public abstract Dao<T> getDao();

    @DatabaseField(generatedId = true)
    protected int id;

    @DatabaseField(index = true)
    protected int serverId = -1;

    @DatabaseField
    protected boolean synced = false;

    private boolean deleted = false;

    public int getId(){
        return id;
    }

    public boolean isSynced(){
        return synced;
    }

    public boolean wasDeleted(){
         return deleted;
    }

    @SuppressWarnings("unchecked")
    public void save(@Nullable final Dao.SingleQueryCallback<T> callback){
        getDao().save((T) this, new Dao.SingleQueryCallback<T>() {
            @Override
            public void onResult(T result) {
                setChanged();
                notifyObservers();
                if(callback!=null) callback.onResult(result);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void delete(@Nullable final Dao.SingleQueryCallback<T> callback){
        getDao().delete((T) this, new Dao.SingleQueryCallback<T>() {
            @Override
            public void onResult(T result) {
                deleted = true;
                setChanged();
                notifyObservers();
                if(callback!=null) callback.onResult(result);
            }
        });
    }

    public abstract JSONObject toJSON() throws JSONException;
}
