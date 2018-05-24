package com.codificador.githubusersearch;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.codificador.githubusersearch.model.GitHubRepo;
import com.codificador.githubusersearch.retrofit.GitHubClient;

import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GitHubRepoAdapter adapter = new GitHubRepoAdapter();
    private Subscription subscription;

    SearchView searchView = null;
    MenuItem searchMenuItem = null;
    ProgressDialog progressDialog;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ListView listView = findViewById(R.id.list_view_repos);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(""+adapter.getItem(i).htmlUrl));
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override protected void onDestroy() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    private void getStarredRepos(String username) {
        subscription = GitHubClient.getInstance()
                .getStarredRepos(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<GitHubRepo>>() {
                    @Override public void onCompleted() {
                        Log.d(TAG, "In onCompleted()");
                        if(progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "In onError()");
                    }

                    @Override public void onNext(List<GitHubRepo> gitHubRepos) {
                        Log.d(TAG, "In onNext() :: "+gitHubRepos.size());
                        adapter.setGitHubRepos(gitHubRepos);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView)searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("enter github user name");
        searchView.setOnQueryTextListener(new GitHubQueryListener());
        return true;
    }

    class GitHubQueryListener implements SearchView.OnQueryTextListener{
        @Override
        public boolean onQueryTextSubmit(String query) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("searching for \""+query+"\"");
            progressDialog.show();
            getStarredRepos(query);
            if(searchMenuItem != null)
                searchMenuItem.collapseActionView();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}