public class ProcessedPost {

    private Long annotationTimeMillis;
    private RawPost originalPost;
    private Boolean isAnnotated;

    public ProcessedPost(RawPost post, Boolean isAnnotated) {
        this.originalPost = post;
        this.isAnnotated = isAnnotated;
        this.annotationTimeMillis = System.currentTimeMillis();
    }

    public Long getAnnotatedTimestamp() {
        return annotationTimeMillis;
    }

    public Boolean isAnnotated() {
        return isAnnotated;
    }

}
