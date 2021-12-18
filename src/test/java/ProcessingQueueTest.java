import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessingQueueTest {

    public static final ArrayList<ProcessedPost> EMPTY_LIST = new ArrayList<>();
    private ProcessingQueue processingQueue;
    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random();
        processingQueue = new ProcessingQueue(new ConcurrentLinkedDeque<>(), 100L);
    }

    @Test
    public void shouldReturnAsCompleteQuoteAvailableByDefault() {
        Integer remainingLinkedInPosts = this.processingQueue.remainingQuotaForPostType(PostType.LINKEDIN);

        assertEquals(remainingLinkedInPosts, 10);
    }

    @Test
    public void shouldReturnTheQuotaValueWhenRemaining() {
        this.processingQueue.processPost(new RawPost(PostType.LINKEDIN, "I had an epiphany!"));
        Integer remainingLinkedInPosts = this.processingQueue.remainingQuotaForPostType(PostType.LINKEDIN);

        assertEquals(remainingLinkedInPosts, 9);
    }

    @Test
    public void shouldReturnZeroWhenQuotaHasBeenExhausted() {
        IntStream.range(0, 10).forEach(index -> this.processingQueue.processPost(new RawPost(PostType.LINKEDIN, "I had an epiphany!" + index)));
        Integer remainingLinkedInPosts = this.processingQueue.remainingQuotaForPostType(PostType.LINKEDIN);

        assertEquals(remainingLinkedInPosts, 0);
    }

    @Test
    public void shouldNotReturnANegativeQuota() {
        IntStream.range(0, 100).forEach(index -> this.processingQueue.processPost(new RawPost(PostType.LINKEDIN, "I had an epiphany!" + index)));
        Integer remainingLinkedInPosts = this.processingQueue.remainingQuotaForPostType(PostType.LINKEDIN);

        assertEquals(remainingLinkedInPosts, 0);
    }

    @Test
    void shouldResetLimitForRespectiveTypesAsTimeWindowShifts() {
        List<ProcessedPost> processedPostList = processNewPosts(1000);

        postsThatWereAnnotated(processedPostList).forEach(post -> {
            Map<PostType, List<ProcessedPost>> postTypeCounts = annotationCountWithinHundredMillis(processedPostList, post.getAnnotatedTimestamp());
            assertTrue(postTypeCounts.getOrDefault(PostType.LINKEDIN, EMPTY_LIST).size() <= PostType.LINKEDIN.getLimit());
            assertTrue(postTypeCounts.getOrDefault(PostType.FB, EMPTY_LIST).size() <= PostType.FB.getLimit());
            assertTrue(postTypeCounts.getOrDefault(PostType.TWEET, EMPTY_LIST).size() <= PostType.TWEET.getLimit());
        });
    }

    private List<ProcessedPost> processNewPosts(int postCount) {
        final List<ProcessedPost> processedPostList = new ArrayList<>();
        IntStream.range(0, postCount).forEach(index -> {
            PostType type = PostType.values()[random.nextInt(3)];
            processedPostList.add(this.processingQueue.processPost(new RawPost(type, "I had an epiphany!" + index)));
        });
        return processedPostList;
    }

    private Map<PostType, List<ProcessedPost>> annotationCountWithinHundredMillis(List<ProcessedPost> processedPostList, Long startTime) {
        return processedPostList
                .stream()
                .filter(ProcessedPost::isAnnotated)
                .filter(e -> e.annotatedAfter(startTime - 1) && e.annotatedBefore(startTime + 101))
                .collect(groupingBy(ProcessedPost::getType));
    }

    private Stream<ProcessedPost> postsThatWereAnnotated(List<ProcessedPost> processedPostList) {
        return processedPostList.stream().filter(ProcessedPost::isAnnotated);
    }
}