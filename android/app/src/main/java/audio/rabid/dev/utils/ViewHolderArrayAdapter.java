package audio.rabid.dev.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.List;

import audio.rabid.dev.network_orm.TypedObservable;
import audio.rabid.dev.network_orm.ViewHolder;

/**
 * Created by charles on 10/25/15.
 */
public abstract class ViewHolderArrayAdapter<T extends TypedObservable<T>, H extends ViewHolder<T>> extends EasyArrayAdapter<T, H> {

    public ViewHolderArrayAdapter(Context context, int layoutId, @Nullable List<T> list) {
        super(context, layoutId, list);
    }

    @Override
    protected void onDrawView(T object, H viewHolder, View parent) {
        viewHolder.setItem(object);
    }
}
