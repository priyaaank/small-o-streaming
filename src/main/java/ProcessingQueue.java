import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProcessingQueue {

    public static final long CURRENT_TIME_WINDOW = 0L;
    private final Long timeWindowMillis;
    private final Map<PostType, ProcessedPostLinkedListNode> nodeMap = new HashMap<>() {
        {
            put(PostType.LINKEDIN, null);
            put(PostType.FB, null);
            put(PostType.TWEET, null);
        }
    };

    public ProcessingQueue(Long timeWindowMillis) {
        this.timeWindowMillis = timeWindowMillis;
    }

    public ProcessedPost process(RawPost post) {
        PostType postType = post.getType();
        Long totalRemainingQuota = postType.getRemainingQuota(nodeMap.get(postType), timeWindowMillis, postType);
        ProcessedPost processedPost = (totalRemainingQuota > 0) ? post.annotate() : post.skipAnnotation();
        nodeMap.put(postType, new ProcessedPostLinkedListNode(nodeMap.get(postType), processedPost));
        return processedPost;
    }

    class ProcessedPostLinkedListNode {

        private ProcessedPostLinkedListNode prevNode;
        private ProcessedPost post;

        public ProcessedPostLinkedListNode(ProcessedPostLinkedListNode prev, ProcessedPost post) {
            this.prevNode = prev;
            this.post = post;
        }

        public Long computeQuota(Long timeWindowMillis, Map<Long, Integer> quotaBuckets, PostType type, long currTimeMillis) {
            if (this.post.isAnnotated()) adjustQuota(timeWindowMillis, quotaBuckets, type, currTimeMillis);
            if (oldestPostProcessingRequest()) return overallPastPresentQuota(quotaBuckets, type);
            return this.prevNode.computeQuota(timeWindowMillis, quotaBuckets, type, currTimeMillis);
        }

        private boolean oldestPostProcessingRequest() {
            return prevNode == null;
        }

        private long overallPastPresentQuota(Map<Long, Integer> quotaBuckets, PostType type) {
            Integer ongoingBurstQuota = quotaBuckets.getOrDefault(CURRENT_TIME_WINDOW, type.getLimit());
            long spilloverQuota = calculateSpillOverQuotaUsage(quotaBuckets);
            return (ongoingBurstQuota + applyCeilingToSpilloverQuota(type, spilloverQuota));
        }

        private long calculateSpillOverQuotaUsage(Map<Long, Integer> quotaBuckets) {
            return quotaBuckets
                    .keySet()
                    .stream()
                    .filter(key -> !Objects.equals(key, CURRENT_TIME_WINDOW)).
                    map(quotaBuckets::get)
                    .map(quotaUsage -> quotaUsage < 0 ? 0 : quotaUsage)
                    .collect(Collectors.summarizingLong(Integer::intValue))
                    .getSum();
        }

        private void adjustQuota(Long timeWindowMillis, Map<Long, Integer> quotaBuckets, PostType type, long currTimeMillis) {
            Long bucket = calculateTimeWindowBucket(timeWindowMillis, currTimeMillis);
            quotaBuckets.put(bucket, quotaBuckets.getOrDefault(bucket, type.getLimit()) - 1);
        }

        private long applyCeilingToSpilloverQuota(PostType type, long spilloverQuota) {
            return spilloverQuota > type.getLimit() ? type.getLimit() : spilloverQuota;
        }

        private long calculateTimeWindowBucket(Long timeWindowMillis, long currTimeMillis) {
            return (currTimeMillis - this.post.getAnnotatedTimestamp()) / timeWindowMillis;
        }
    }
}
