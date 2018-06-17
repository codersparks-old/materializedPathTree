package uk.codersparks.communitytreematerializedpath.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.codersparks.communitytreematerializedpath.model.Community;
import uk.codersparks.communitytreematerializedpath.repository.CommunityRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@RunWith(SpringJUnit4ClassRunner.class)
public class CommunityServiceTest {

    @MockBean
    private CommunityRepository repository;

    @Captor
    private ArgumentCaptor<List<Community>> communityListCaptor;

    private CommunityService communityService;

    private String com1 = "com1";
    private String com2 = "com2";
    private String com3 = "com3";
    private String com4 = "com4";
    private String com5 = "com5";
    private String com6 = "com6";
    private String com7 = "com7";

    // Hierachy
    //  root
    //      com1
    //          com7
    //      com2
    //          com3
    //              com4
    //                  com5
    //                  com6


    private String com1Parent = "/";
    private String com2Parent = "/";
    private String com3Parent = "/com2/";
    private String com4Parent = "/com2/com3/";
    private String com5Parent = "/com2/com3/com4/";
    private String com6Parent = "/com2/com3/com4/";
    private String com7Parent = "/com1/";

    private Community c1,c2,c3,c4,c5,c6, c7;


    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        communityService = new CommunityService(repository);

        c1 = new Community(com1, com1Parent);
        c2 = new Community(com2, com2Parent);
        c3 = new Community(com3, com3Parent);
        c4 = new Community(com4, com4Parent);
        c5 = new Community(com5, com5Parent);
        c6 = new Community(com6, com6Parent);
        c7 = new Community(com7, com7Parent);


        given(repository.save(any(Community.class))).willAnswer(i -> i.getArguments()[0]);
        given(repository.findAllByOrderByPathAsc()).willReturn(new ArrayList<>(Arrays.asList(c1,c2,c3,c4,c5,c6,c7)));

    }

    @Test
    public void createCommunity_rootNode_NullParent() {

        Community community = communityService.createCommunity(com1, null);

        assertThat(community.getName()).isEqualTo(com1);
        assertThat(community.getPath()).isEqualTo(Community.PATH_SEPERATOR);

        verify(repository).findOne(com1);
        verify(repository).save(any(Community.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void createCommunity_rootNode_EmptyParent() {

        Community community = communityService.createCommunity(com1, "");

        assertThat(community.getName()).isEqualTo(com1);
        assertThat(community.getPath()).isEqualTo(Community.PATH_SEPERATOR);

        verify(repository).findOne(com1);
        verify(repository).save(any(Community.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void createCommunity_singleParent() {

        Community community = new Community(com1, "/");
        given(repository.findOne(com1)).willReturn(community);

        Community community2 = communityService.createCommunity(com2, com1);

        assertThat(community2.getName()).isEqualTo(com2);
        assertThat(community2.getPath()).isEqualTo(community.getPath() + community.getName() + Community.PATH_SEPERATOR);

        verify(repository).findOne(com1);
        verify(repository).findOne(com2);
        verify(repository).save(any(Community.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void moveCommunity_noSubTree() {

        given(repository.findOne(com7)).willReturn(c7);
        given(repository.findOne(com2)).willReturn(c2);

        Community movedCommunity = communityService.moveCommunity(com7, com2);

        assertThat(movedCommunity.getName()).isEqualTo(com7);
        assertThat(movedCommunity.getPath()).isEqualTo(c2.getIdentityPath());

        verify(repository).findOne(com7);
        verify(repository).findOne(com2);
        verify(repository).findAllByOrderByPathAsc();
        verify(repository).save(any(Community.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void moveCommunity_withSubTree_nonRootMove() {

        given(repository.findOne(com3)).willReturn(c3);
        given(repository.findOne(com1)).willReturn(c1);

        Community movedCommunity = communityService.moveCommunity(com3, com1);

        assertThat(movedCommunity.getName()).isEqualTo(com3);
        assertThat(movedCommunity.getPath()).isEqualTo(c1.getIdentityPath());

        verify(repository).findOne(com3);
        verify(repository).findOne(com1);
        verify(repository).findAllByOrderByPathAsc();
        verify(repository).save(communityListCaptor.capture());
        verify(repository).save(c3);
        verifyNoMoreInteractions(repository);

        List<Community> modifiedCommunities = communityListCaptor.getValue();

        assertThat(modifiedCommunities).hasSize(3);

        assertThat(modifiedCommunities.stream().map(Community::getName).collect(Collectors.toList())).containsExactlyInAnyOrder(com4,com5,com6);

        modifiedCommunities.stream().forEach(c -> {
            assertThat(c.getPath().startsWith(c3.getIdentityPath()));
        });


    }


    @Test
    public void moveCommunity_withSubTree_moveToRoot() {

        given(repository.findOne(com3)).willReturn(c3);
        given(repository.findOne(com1)).willReturn(c1);

        Community movedCommunity = communityService.moveCommunity(com3, "");

        assertThat(movedCommunity.getName()).isEqualTo(com3);
        assertThat(movedCommunity.getPath()).isEqualTo("/");

        verify(repository).findOne(com3);
        verify(repository).findAllByOrderByPathAsc();
        verify(repository).save(communityListCaptor.capture());
        verify(repository).save(c3);
        verifyNoMoreInteractions(repository);

        List<Community> modifiedCommunities = communityListCaptor.getValue();

        assertThat(modifiedCommunities).hasSize(3);

        assertThat(modifiedCommunities.stream().map(Community::getName).collect(Collectors.toList())).containsExactlyInAnyOrder(com4,com5,com6);

        modifiedCommunities.stream().forEach(c -> {
            assertThat(c.getPath().startsWith(c.getIdentityPath()));
        });


    }

    @Test
    public void distanceToCommonAncestor() {

        // Hierachy
        //  root
        //      com1
        //          com7
        //      com2
        //          com3
        //              com4
        //                  com5
        //                  com6

        int c1Toc1 = communityService.distanceToCommonAncestor(c1, c1);

        assertThat(c1Toc1).isEqualTo(0);

        int c6Toc7 = communityService.distanceToCommonAncestor(c6, c7);

        assertThat(c6Toc7).isEqualTo(4);

        int c7Toc6 = communityService.distanceToCommonAncestor(c7, c6);

        assertThat(c7Toc6).isEqualTo(2);

        int c6Toc5 = communityService.distanceToCommonAncestor(c6,c5);

        assertThat(c6Toc5).isEqualTo(1);
    }



}