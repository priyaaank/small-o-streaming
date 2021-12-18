public enum PostType {

    TWEET(60),
    LINKEDIN(10),
    FB(30);

    private int postCount;

    PostType(int postCount) {
        this.postCount = postCount;
    }

    public Integer getLimit() {
        return postCount;
    }

}
