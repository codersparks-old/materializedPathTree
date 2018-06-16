package uk.codersparks.communitytreematerializedpath.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import uk.codersparks.communitytreematerializedpath.model.Community;
import uk.codersparks.communitytreematerializedpath.service.CommunityService;

@Configuration
public class CommunitySeedingCommandLineRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CommunitySeedingCommandLineRunner.class);

    private final MongoOperations mongoOperations;
    private final CommunityService communityService;

    public CommunitySeedingCommandLineRunner(MongoOperations mongoOperations, CommunityService communityService) {
        this.mongoOperations = mongoOperations;
        this.communityService = communityService;
    }

    @Override
    public void run(String... strings) throws Exception {

        mongoOperations.getCollectionNames().forEach(name -> mongoOperations.dropCollection(name));

        Community community1 = communityService.createCommunity("com1", "");
        Community community1_1 = communityService.createCommunity("com1_1", "com1");
        Community community1_2 = communityService.createCommunity("com1_2", "com1");
        Community community1_3 = communityService.createCommunity("com1_3", "com1");

        Community community1_2_1 = communityService.createCommunity("com1_2_1", "com1_2");
        Community community1_2_2 = communityService.createCommunity("com1_2_2", "com1_2");
        Community community1_2_3 = communityService.createCommunity("com1_2_3", "com1_2");
        Community community1_2_4 = communityService.createCommunity("com1_2_4", "com1_2");


        Community community2 = communityService.createCommunity("com2", null);
        Community community2_1 = communityService.createCommunity("com2_1", "com2");
        Community community2_2 = communityService.createCommunity("com2_2", "com2");
        Community community2_3 = communityService.createCommunity("com2_3", "com2");

        Community community2_1_1 = communityService.createCommunity("com2_1_1", "com2_1");
        Community community2_1_2 = communityService.createCommunity("com2_1_2", "com2_1");
        Community community2_1_3 = communityService.createCommunity("com2_1_3", "com2_1");
        Community community2_1_4 = communityService.createCommunity("com2_1_4", "com2_1");

        logger.info("Communities: {}", communityService.getAllCommunities());
    }
}
