package audio.rabid.dev.roe.models.rails;

import android.support.annotation.Nullable;

import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import audio.rabid.dev.roe.models.Server;

/**
 * Created by charles on 10/29/15.
 * <p>
 * A server which expects standard Rails `resources` endpoints. Specifically, the pluralized endpoints
 * except for `new` and `edit`.
 * </p>
 * <p>
 * E.g. `resources :posts`:
 * </p>
 * <pre>
 * index (return multiple posts, possibly with query parameters to filter) - GET /posts => { posts: [ {...}, ...] }
 * show (return single post by id) - GET /posts/:id => { post: {...} }
 * create (create new post) - POST /posts with { post: {...} } => { post: {...} }
 * update (update existing) - PUT/PATCH /posts/:id with { post: {...} } => { post: {...} }
 * delete (delete existing) - DELETE /posts/:id => { post: {...} }
 * </pre>
 */
public class RailsServer extends Server {

    private Map<Class, String> endpoints;

    public RailsServer(String rootURL) {
        super(rootURL);
        endpoints = new HashMap<>();
    }

    public RailsServer(String rootURL, Map<Class, String> endpoints) {
        super(rootURL);
        this.endpoints = endpoints;
    }

    public void addEndpoint(Class clazz, String endpoint) {
        endpoints.put(clazz, endpoint);
    }

    public Response index(String endpoint, @Nullable JSONObject search) throws NetworkException {
        return request(endpoint, Method.GET, search);
    }

    public Response show(String endpoint, String serverId) throws NetworkException {
        return request(endpoint + "/" + serverId, Method.GET, null);
    }

    public Response create(String endpoint, JSONObject data) throws NetworkException {
        return request(endpoint, Method.POST, data);
    }

    public Response update(String endpoint, String serverId, JSONObject data) throws NetworkException {
        return request(endpoint + "/" + serverId, Method.PUT, data);
    }

    public Response destroy(String endpoint, String serverId) throws NetworkException {
        return request(endpoint + "/" + serverId, Method.DELETE, null);
    }

    @Override
    public boolean isErrorResponse(Response response) {
        return (response.getResponseCode() / 100 != 2) || !response.getResponseBody().isNull("error");
    }

    @Override
    public Response getItem(Class<?> clazz, String serverId) throws NetworkException {
        return show(getEndpoint(clazz), serverId);
    }

    @Override
    public Response createItem(Class<?> clazz, JSONObject item) throws NetworkException {
        return create(getEndpoint(clazz), item);
    }

    @Override
    public Response getItems(Class<?> clazz, JSONObject search) throws NetworkException {
        return index(getEndpoint(clazz), search);
    }

    @Override
    public Response updateItem(Class<?> clazz, String serverId, JSONObject data) throws NetworkException {
        return update(getEndpoint(clazz), serverId, data);
    }

    @Override
    public Response deleteItem(Class<?> clazz, String serverId) throws NetworkException {
        return destroy(getEndpoint(clazz), serverId);
    }

    private String getEndpoint(Class<?> clazz) {
        String endpoint = endpoints.get(clazz);
        if (endpoint == null) {
            try {
                //try to get from annotation
                RailsResource railsResource = clazz.getAnnotation(RailsResource.class);
                if (railsResource != null) {
                    if (!railsResource.endpoint().isEmpty()) {
                        endpoint = railsResource.endpoint();
                    } else if (!railsResource.pluralJSONKey().isEmpty()) {
                        endpoint = railsResource.pluralJSONKey();
                    }
                } else {
                    //try to get from Resource table name
                    DatabaseTable table = clazz.getAnnotation(DatabaseTable.class);
                    if (table != null) {
                        endpoint = table.tableName();
                    } else {
                        endpoint = clazz.getSimpleName().toLowerCase();
                    }
                }
                endpoints.put(clazz, endpoint);
            } catch (Exception e) {
                throw new NullPointerException("Endpoint was never sent to RailsServer and couldn't be inferred from Resource");
            }
        }
        if(endpoint==null){
            throw new NullPointerException("Endpoint was never sent to RailsServer and couldn't be inferred from Resource");
        }
        return endpoint;
    }
}
