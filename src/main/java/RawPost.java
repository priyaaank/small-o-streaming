import lombok.Getter;

@Getter
public class RawPost {

    private PostType type;
    private String body;

    public RawPost(PostType type, String body) {
        this.type = type;
        this.body = body;
    }

    public ProcessedPost annotate() {
        return new ProcessedPost(this, Boolean.TRUE);
    }

    public ProcessedPost skipAnnotation() {
        return new ProcessedPost(this, Boolean.FALSE);
    }

}
