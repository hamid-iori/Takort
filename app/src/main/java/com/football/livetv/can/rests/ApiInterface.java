package com.football.livetv.can.rests;




import com.football.livetv.can.callbacks.CallbackCategories;
import com.football.livetv.can.callbacks.CallbackChannel;
import com.football.livetv.can.callbacks.CallbackChannelDetail;
import com.football.livetv.can.callbacks.CallbackDetailCategory;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: The Stream";

    @Headers({CACHE, AGENT})
    @GET("api.php?get_posts")
    Single<CallbackChannel> getRecentChannel(
            @Query("page") int page,
            @Query("count") int count,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_category_posts")
    Single<CallbackDetailCategory> getChannelByCategory(
            @Query("id") int id,
            @Query("page") int page,
            @Query("count") int count,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_category_index")
    Single<CallbackCategories> getAllCategories(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_search_results")
    Single<CallbackChannel> getSearchChannel(
            @Query("search") String search,
            @Query("count") int count,
            @Query("api_key") String api_key
    );


    @Headers({CACHE, AGENT})
    @GET("api.php?get_post_detail")
    Single<CallbackChannelDetail> getChannelDetail(
            @Query("id") String id
    );


}
