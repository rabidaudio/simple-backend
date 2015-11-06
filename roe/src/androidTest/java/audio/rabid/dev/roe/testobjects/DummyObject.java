package audio.rabid.dev.roe.testobjects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import audio.rabid.dev.roe.models.IntegerKeyedNetworkResource;
import audio.rabid.dev.roe.models.JSONField;
import audio.rabid.dev.roe.models.Source;

/**
 * Created by charles on 10/30/15.
 */
@DatabaseTable(tableName = "dummies")
public class DummyObject extends IntegerKeyedNetworkResource<DummyObject> {

    public DummyObject() {
    }

    public DummyObject(String name, int age, DummyChild child) {
        this.name = name;
        this.age = age;
        this.child = child;
    }

    @JSONField
    @DatabaseField
    protected String name;

    @JSONField
    @DatabaseField
    protected int age = 0;

    @JSONField
    @DatabaseField(foreign = true)
    public DummyChild child;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age){
        this.age = age;
    }

    public DummyChild getChild() {
        return child;
    }

    @Override
    public Source<DummyObject, Integer> getSource() {
        return DummyObjectSource.getInstance();
    }
}
