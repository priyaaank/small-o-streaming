import java.util.HashMap;

public enum PostType {

    TWEET(60),
    LINKEDIN(10),
    FB(30);

    private int defaultQuota;
    private ProcessingQueue.ProcessedPostLinkedListNode postNode;
    private String typeStr;

    PostType(int defaultQuota) {
        this.defaultQuota = defaultQuota;
    }

    public Integer getLimit() {
        return defaultQuota;
    }

    public Long getRemainingQuota(ProcessingQueue.ProcessedPostLinkedListNode postLinkedListNode, Long timeWindowMillis, PostType type) {
        if (postLinkedListNode == null) return (long) this.defaultQuota;
        return postLinkedListNode.computeQuota(timeWindowMillis, new HashMap<>(), type, System.currentTimeMillis());
    }

}
