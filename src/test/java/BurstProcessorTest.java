import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BurstProcessorTest {

    private BurstController processingQueue;
    private Integer index;

    @BeforeEach
    void setUp() {
        index = 0;
        this.processingQueue = new BurstController(100L);
    }

    @Test
    public void shouldProcessAllPostsWhenWithinQuota() {
        List<ProcessedPost> processedPosts = processPosts(5, PostType.LINKEDIN);

        assertEquals(5, processedPosts.stream().filter(ProcessedPost::isAnnotated).count());
    }

    @Test
    public void shouldManageQuotaSeparatelyForDifferentPostType() {
        List<ProcessedPost> processedPosts = processPosts(10, PostType.LINKEDIN);
        processedPosts.addAll(processPosts(60, PostType.TWEET));

        assertEquals(70, processedPosts.stream().filter(ProcessedPost::isAnnotated).count());
    }

    @Test
    public void shouldAnnotatePostsWithinQuotaLimitsDuringBursts() {
        List<ProcessedPost> processedPosts = processPosts(100, PostType.LINKEDIN);

        assertEquals(10, processedPosts.stream().filter(ProcessedPost::isAnnotated).count());
    }

    @Test
    void shouldResetQuotaWhenTimeWindowElapses() throws InterruptedException {
        List<ProcessedPost> processedPostList = processPosts(100, PostType.LINKEDIN);
        Thread.sleep(100);
        processedPostList.addAll(processPosts(100, PostType.LINKEDIN));

        assertEquals(20, processedPostList.stream().filter(ProcessedPost::isAnnotated).count());
    }

    @Test
    void shouldCarryOverQuotaDuringUnevenBurst() throws InterruptedException {
        List<ProcessedPost> processedPostList = new ArrayList<>();
        processedPostList.addAll(processPosts(100, PostType.LINKEDIN));
        processedPostList.addAll(processPosts(100, PostType.TWEET));
        processedPostList.addAll(processPosts(100, PostType.FB));
        Thread.sleep(100);

        processedPostList.addAll(processPosts(1, PostType.LINKEDIN));
        processedPostList.addAll(processPosts(1, PostType.TWEET));
        processedPostList.addAll(processPosts(1, PostType.FB));
        Thread.sleep(100);

        processedPostList.addAll(processPosts(25, PostType.LINKEDIN));
        processedPostList.addAll(processPosts(125, PostType.TWEET));
        processedPostList.addAll(processPosts(65, PostType.FB));

        assertEquals(300, processedPostList.stream().filter(ProcessedPost::isAnnotated).count());
    }

    private List<ProcessedPost> processPosts(int count, PostType type) {
        List<ProcessedPost> processedPostList = new ArrayList<>();
        IntStream.range(0, count).forEach(idx -> processedPostList.add(this.processingQueue.process(dummyPost(type))));
        return processedPostList;
    }

    private RawPost dummyPost(PostType type) {
        return new RawPost(type, "I had an epiphany!" + index++);
    }

}
