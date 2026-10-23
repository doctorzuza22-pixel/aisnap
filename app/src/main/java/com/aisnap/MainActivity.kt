<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000000">

    <androidx.camera.view.PreviewView
        android:id="@+id/previewView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="top"
        android:orientation="horizontal"
        android:padding="12dp">

        <Button android:id="@+id/flashButton" android:text="FLASH" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
        <Button android:id="@+id/switchCameraButton" android:text="FLIP" android:layout_width="wrap_content" android:layout_height="wrap_content" android:layout_marginStart="8dp"/>
    </LinearLayout>

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:orientation="horizontal">
        <Button android:id="@+id/zoom05" android:text="0.5x" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
        <Button android:id="@+id/zoom1x" android:text="1x" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
        <Button android:id="@+id/zoom2x" android:text="2x" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
        <Button android:id="@+id/zoom5x" android:text="5x" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        android:orientation="vertical"
        android:padding="16dp">
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center">
            <Button android:id="@+id/modePhoto" android:text="PHOTO" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
            <Button android:id="@+id/modeVideo" android:text="VIDEO" android:layout_width="wrap_content" android:layout_height="wrap_content" android:layout_marginStart="12dp"/>
        </LinearLayout>
        <Button android:id="@+id/captureButton" android:text="CAPTURE" android:layout_width="150dp" android:layout_height="150dp" android:layout_gravity="center" android:layout_marginTop="12dp"/>
    </LinearLayout>
</FrameLayout>
