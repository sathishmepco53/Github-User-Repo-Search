package com.codificador.githubusersearch.retrofit;

import com.codificador.githubusersearch.model.GitHubRepo;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Seng on 5/22/2018.
 */

public interface GitHubService {
//    https://api.github.com/users/arriolac/starred
    /*@GET("users/{user}/starred")
    Observable<List<GitHubRepo>> getStarredRepositories(@Path("user") String userName);*/

//    https://api.github.com/users/sathishmepco53
    @GET("users/{user}/repos")
    Observable<List<GitHubRepo>> getRepositories(@Path("user") String userName);
}
