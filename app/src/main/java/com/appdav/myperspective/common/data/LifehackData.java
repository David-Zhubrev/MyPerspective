package com.appdav.myperspective.common.data;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class LifehackData {

    /**
     * Public empty constructor required by Firebase
     * This constructor SHOULD NOT be used on the clientside
     * Use Builder class instead to create new LifehackData object
     */

    public LifehackData() {

    }

    private String lifehackId;

    private ArrayList<String> likes;

    private String content;

    private String authorUid;
    @Exclude
    private String authorText;

    @Exclude
    public String getAuthorText() {
        return authorText;
    }

    @Exclude
    public void setAuthorText(String authorText) {
        this.authorText = authorText;
    }

    @Exclude
    private String photoUri;

    @Exclude
    private boolean isLikedByCurrentUser;

    @Exclude
    public boolean isLikedByCurrentUser() {
        return isLikedByCurrentUser;
    }

    @Exclude
    public void setLikedByCurrentUser(boolean isLikedByCurrentUser) {
        this.isLikedByCurrentUser = isLikedByCurrentUser;
    }

    public String getLifehackId() {
        return lifehackId;
    }

    public void setLifehackId(String lifehackId) {
        this.lifehackId = lifehackId;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    @Exclude
    public String getPhotoUri() {
        return photoUri;
    }

    @Exclude
    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        else if (!(obj instanceof LifehackData)) return false;
        else return ((LifehackData) obj).lifehackId.equals(this.lifehackId);
    }

    public void addLike(String userId) {
        if (likes == null) likes = new ArrayList<>();
        if (likes.contains(userId)) return;
        likes.add(userId);
    }

    public void removeLike(String userId) {
        if (likes == null) return;
        likes.remove(userId);
    }

    public boolean likeArrayDiffersFrom(LifehackData lifehackData) {
        if (this.likes == null) {
            return lifehackData.likes != null;
        } else {
            if (lifehackData.likes == null) return true;
            else if (likes.size() != lifehackData.likes.size()) return true;
            else {
                for (String s : likes) {
                    if (!lifehackData.likes.contains(s)) return true;
                }
                return false;
            }
        }
    }

    public static class Builder {

        private String content = "";
        private String authorUid = "";

        public Builder() {
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder author(String authorUid) {
            this.authorUid = authorUid;
            return this;
        }


        public LifehackData build() {
            LifehackData result = new LifehackData();
            result.content = content;
            result.authorUid = authorUid;
            return result;
        }

    }
}
