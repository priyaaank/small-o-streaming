public class ProcessedPost {

    private Long annotationTimeMillis;
    private RawPost originalPost;
    private Boolean isAnnotated;

    public ProcessedPost(RawPost post, Boolean isAnnotated) {
        this.originalPost = post;
        this.isAnnotated = isAnnotated;
        if (isAnnotated) this.annotationTimeMillis = System.currentTimeMillis();
    }

    public Boolean annotatedAfter(Long timeInMillis) {
        return this.annotationTimeMillis > timeInMillis;
    }

    public boolean annotatedBefore(Long annotatedTimestamp) {
        return this.annotationTimeMillis < annotatedTimestamp;
    }

    public Long getAnnotatedTimestamp() {
        return annotationTimeMillis;
    }

    public PostType getType() {
        return this.originalPost.getType();
    }

    public Boolean isAnnotated() {
        return isAnnotated;
    }

}
