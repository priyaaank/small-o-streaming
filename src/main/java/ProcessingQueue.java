
import lombok.extern.slf4j.Slf4j;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Slf4j
public class ProcessingQueue {

    private Deque<ProcessedPost> processedPostQueue;
    private Long timeWindowInMillis;

    public ProcessingQueue() {
        this(new ConcurrentLinkedDeque<>(), 60000L);
    }

    public ProcessingQueue(Deque<ProcessedPost> processedPosts, Long timeWindowInMillis) {
        this.processedPostQueue = processedPosts;
        this.timeWindowInMillis = timeWindowInMillis;
    }

    public ProcessedPost processPost(RawPost rawPost) {
        Integer remainingQuota = this.remainingQuotaForPostType(rawPost.getType());
        ProcessedPost processedPost = remainingQuota > 0 ? rawPost.annotate() : rawPost.skipAnnotation();
        this.processedPostQueue.addFirst(processedPost);
        return processedPost;
    }

    public Integer remainingQuotaForPostType(PostType withPostType) {
        Long milliSecondsAgo = System.currentTimeMillis() - timeWindowInMillis;
        List<ProcessedPost> filteredPosts = postsAnnotatedInLast(milliSecondsAgo, withPostType);
        return calculateRemainingQuota(withPostType, filteredPosts);
    }

    private int calculateRemainingQuota(PostType withPostType, List<ProcessedPost> filteredPosts) {
        return filteredPosts.size() >= withPostType.getLimit() ? 0 : withPostType.getLimit() - filteredPosts.size();
    }

    private List<ProcessedPost> postsAnnotatedInLast(Long tenSecondsAgo, PostType withPostType) {
        return processedPostQueue
                .stream()
                .filter(ele -> withPostType.equals(ele.getType()))
                .filter(ProcessedPost::isAnnotated)
                .filter(ele -> ele.annotatedAfter(tenSecondsAgo))
                .collect(Collectors.toList());
    }

}
