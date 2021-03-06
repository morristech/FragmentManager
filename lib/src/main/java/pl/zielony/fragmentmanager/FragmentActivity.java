package pl.zielony.fragmentmanager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2016-05-11.
 */
public class FragmentActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        FragmentRootView rootView = new FragmentRootView(this);
        fragmentManager.setRootView(rootView);
        View.inflate(this, layoutResID, rootView);
        super.setContentView(rootView);
    }

    @Override
    public void setContentView(View view) {
        FragmentRootView rootView = new FragmentRootView(this);
        fragmentManager.setRootView(rootView);
        rootView.addView(view);
        super.setContentView(rootView);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        FragmentRootView rootView = new FragmentRootView(this);
        fragmentManager.setRootView(rootView);
        rootView.addView(view, params);
        super.setContentView(rootView);
    }

    public void enableFragmentDebugging() {
        FragmentTreeView treeView = new FragmentTreeView(this);
        treeView.setFragmentManager(fragmentManager);
        addContentView(treeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.backTraverse())
            return;
        super.onBackPressed();
    }

    public void onUpPressed() {
        if (fragmentManager.upTraverse())
            return;
        super.onBackPressed();
    }

    public void navigate(FragmentRoute route) {
        FragmentRoute.Step step = route.getStep();
        if (step.fragment == null)
            step.fragment = Fragment.instantiate(step.klass, this, null);
        if (onNavigate(route)) {
            if (route.length() == 0)
                return;
            if (fragmentManager.navigate(route))
                return;
        }
        throw new RouteNotHandledException(route);
    }

    public void navigate(Fragment fragment, TransactionMode mode) {
        navigate(new FragmentRoute(fragment, mode));
    }

    public void navigate(Fragment fragment, TransactionMode mode, Fragment target) {
        fragment.setTargetFragment(target);
        navigate(new FragmentRoute(fragment, mode));
    }

    public void navigate(Class<? extends Fragment> klass, TransactionMode mode) {
        navigate(new FragmentRoute(klass, mode));
    }

    protected boolean onNavigate(FragmentRoute route) {
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fragmentManager.save(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fragmentManager.restore(savedInstanceState);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = new FragmentManager(this, savedInstanceState);
    }

    public FragmentManager getFragmentManager2() {
        return fragmentManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        fragmentManager.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fragmentManager.resume();
    }

    @Override
    protected void onPause() {
        fragmentManager.pause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        fragmentManager.stop();
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fragmentManager.dispatchNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragmentManager.dispatchActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> granted = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(permissions[i]);
            } else {
                rejected.add(permissions[i]);
            }
        }
        fragmentManager.dispatchRequestPermissionsResult(requestCode, granted, rejected);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (super.dispatchKeyEvent(event))
            return true;
        return fragmentManager.dispatchKeyEvent(event);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        FragmentPool.clear();
    }
}
