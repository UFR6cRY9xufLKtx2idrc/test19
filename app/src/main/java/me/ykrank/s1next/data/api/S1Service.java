package me.ykrank.s1next.data.api;

import me.ykrank.s1next.data.api.model.Profile;
import me.ykrank.s1next.data.api.model.collection.Favourites;
import me.ykrank.s1next.data.api.model.collection.ForumGroups;
import me.ykrank.s1next.data.api.model.collection.Friends;
import me.ykrank.s1next.data.api.model.collection.Notes;
import me.ykrank.s1next.data.api.model.collection.PmGroups;
import me.ykrank.s1next.data.api.model.collection.Posts;
import me.ykrank.s1next.data.api.model.collection.Threads;
import me.ykrank.s1next.data.api.model.wrapper.AccountResultWrapper;
import me.ykrank.s1next.data.api.model.wrapper.BaseDataWrapper;
import me.ykrank.s1next.data.api.model.wrapper.BaseResultWrapper;
import me.ykrank.s1next.data.api.model.wrapper.PmsWrapper;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface S1Service {

    @GET(ApiForum.URL_FORUM)
    Observable<BaseDataWrapper<ForumGroups>> getForumGroupsWrapper();

    @GET(ApiHome.URL_FAVOURITES)
    Observable<BaseResultWrapper<Favourites>> getFavouritesWrapper(@Query("page") int page);

    @GET(ApiForum.URL_THREAD_LIST)
    Observable<BaseResultWrapper<Threads>> getThreadsWrapper(@Query("fid") String forumId, @Query("page") int page);

    @GET(ApiForum.URL_POST_LIST)
    Observable<BaseResultWrapper<Posts>> getPostsWrapper(@Query("tid") String threadId, @Query("page") int page);

    @GET(Api.URL_QUOTE_POST_REDIRECT)
    Observable<Response<Void>> getQuotePostResponseBody(@Query("ptid") String threadId, @Query("pid") String quotePostId);

    @FormUrlEncoded
    @POST(ApiMember.URL_LOGIN)
    Observable<AccountResultWrapper> login(@Field("username") String username, @Field("password") String password);

    @GET(ApiForum.URL_AUTHENTICITY_TOKEN_HELPER)
    Observable<AccountResultWrapper> refreshAuthenticityToken();

    //region Favourites
    @FormUrlEncoded
    @POST(ApiHome.URL_THREAD_FAVOURITES_ADD)
    Observable<AccountResultWrapper> addThreadFavorite(@Field("formhash") String authenticityToken, @Field("id") String threadId, @Field("description") String remark);

    @FormUrlEncoded
    @POST(ApiHome.URL_THREAD_FAVOURITES_REMOVE)
    Observable<AccountResultWrapper> removeThreadFavorite(@Field("formhash") String authenticityToken, @Field("favid") String favId);
    //endregion

    //region Reply
    @FormUrlEncoded
    @POST(ApiForum.URL_REPLY)
    Observable<AccountResultWrapper> reply(@Field("formhash") String authenticityToken, @Field("tid") String threadId, @Field("message") String reply);

    @GET(Api.URL_QUOTE_HELPER)
    Observable<String> getQuoteInfo(@Query("tid") String threadId, @Query("repquote") String quotePostId);

    @FormUrlEncoded
    @POST(ApiForum.URL_REPLY)
    Observable<AccountResultWrapper> replyQuote(@Field("formhash") String authenticityToken, @Field("tid") String threadId, @Field("message") String reply,
                                                @Field("noticeauthor") String encodedUserId, @Field("noticetrimstr") String quoteMessage, @Field("noticeauthormsg") String replyNotification);
    //endregion

    //<editor-fold desc="PM">
    @GET(ApiHome.URL_PM_LIST)
    Observable<BaseDataWrapper<PmGroups>> getPmGroups(@Query("page") int page);

    @GET(ApiHome.URL_PM_VIEW_LIST)
    Observable<PmsWrapper> getPmList(@Query("touid") String toUid, @Query("page") int page);

    @FormUrlEncoded
    @POST(ApiHome.URL_PM_POST)
    Observable<AccountResultWrapper> postPm(@Field("formhash") String authenticityToken, @Field("touid") String toUid, @Field("message") String msg);
    //</editor-fold>

    //region New thread
    @GET(Api.URL_NEW_THREAD_HELPER)
    Observable<String> getNewThreadInfo(@Query("fid") int fid);

    @FormUrlEncoded
    @POST(ApiForum.URL_NEW_THREAD)
    Observable<AccountResultWrapper> newThread(@Query("fid") int fid, @Field("formhash") String authenticityToken, @Field("posttime") long postTime, @Field("typeid") String typeId,
                                               @Field("subject") String subject, @Field("message") String message, @Field("allownoticeauthor") int allowNoticeAuthor,
                                               @Field("usesig") int useSign, @Field("save") Integer saveAsDraft);
    //endregion

    @FormUrlEncoded
    @POST(Api.URL_SEARCH_FORUM)
    Observable<String> searchForum(@Field("formhash") String authenticityToken, @Field("searchsubmit") String searchSubmit, @Field("srchtxt") String text);

    @GET(ApiHome.URL_MY_NOTE_LIST)
    Observable<BaseDataWrapper<Notes>> getMyNotes(@Query("page") int page);

    @GET(ApiHome.URL_PROFILE)
    Observable<BaseDataWrapper<Profile>> getProfile(@Query("uid") String uid);

    @GET(ApiHome.URL_FRIENDS)
    Observable<BaseDataWrapper<Friends>> getFriends(@Query("uid") String uid);
}
