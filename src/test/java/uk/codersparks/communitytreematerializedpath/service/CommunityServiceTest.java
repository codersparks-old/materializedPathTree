package uk.codersparks.communitytreematerializedpath.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.codersparks.communitytreematerializedpath.model.Community;
import uk.codersparks.communitytreematerializedpath.repository.CommunityRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;


@RunWith(SpringJUnit4ClassRunner.class)
public class CommunityServiceTest {

    @MockBean
    private CommunityRepository repository;

    private CommunityService communityService;

    String com1 = "com1";
    String com2 = "com2";
    String com3 = "com3";
    String com4 = "com4";


    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        communityService = new CommunityService(repository);

        given(repository.save(any(Community.class))).willAnswer(i -> i.getArguments()[0]);

    }

    @Test
    public void createCommunity_rootNode_NullParent() {

        Community community = communityService.createCommunity(com1, null);

        assertThat(community.getName()).isEqualTo(com1);
        assertThat(community.getPath()).isEqualTo(Community.PATH_SEPERATOR);

        verify(repository).save(any(Community.class));
    }

    @Test
    public void createCommunity_rootNode_EmptyParent() {

        Community community = communityService.createCommunity(com1, "");

        assertThat(community.getName()).isEqualTo(com1);
        assertThat(community.getPath()).isEqualTo(Community.PATH_SEPERATOR);

        verify(repository).save(any(Community.class));
    }

    @Test
    public void createCommunity_singleParent() {

        Community community = new Community(com1, "/");
        given(repository.findOne(com1)).willReturn(community);

        Community community2 = communityService.createCommunity(com2, com1);

        assertThat(community2.getName()).isEqualTo(com2);
        assertThat(community2.getPath()).isEqualTo(community.getPath() + community.getName() + Community.PATH_SEPERATOR);

        verify(repository).save(any(Community.class));
    }

    @Test
    public void moveCommunity_newParent() {

        Community community1 = new Community(com1, "/");

        given(repository.findOne(com1)).willReturn(community1);

        Community community2 = new Community(com2, "/some/parent/");

        given(repository.findOne(com2)).willReturn(community2);

        community2 = communityService.moveCommunity(com2, com1);

        assertThat(community2.getName()).isEqualTo(com2);
        assertThat(community2.getPath()).isEqualTo(Community.PATH_SEPERATOR + com1 + Community.PATH_SEPERATOR);

        verify(repository).save(any(Community.class));


    }

}