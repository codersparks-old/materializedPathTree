package uk.codersparks.communitytreematerializedpath.service;

import ch.qos.logback.core.joran.node.ComponentNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.codersparks.communitytreematerializedpath.model.Community;
import uk.codersparks.communitytreematerializedpath.model.CommunityTreeNode;
import uk.codersparks.communitytreematerializedpath.repository.CommunityRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    private static final Logger logger = LoggerFactory.getLogger(CommunityService.class);

    private static final Pattern ROOT_PATH_PATTERN = Pattern.compile("^" + Community.PATH_SEPERATOR + "$");
    //private static final Pattern IMMEDIATE_PARENT_PATTERN = Pattern.compile(Community.PATH_SEPERATOR + "([^" + Community.PATH_SEPERATOR + "]+)" + Community.PATH_SEPERATOR + "&");
    private static final Pattern IMMEDIATE_PARENT_PATTERN = Pattern.compile("/([^/]+)/$");

    private final CommunityRepository repository;

    public CommunityService(CommunityRepository repository) {
        this.repository = repository;
    }


    public Community getCommunity(String name) {
        return this.repository.findOne(name);
    }

    public List<Community> getAllCommunities() {
        return repository.findAllByOrderByPathAsc();
    }

    public Community createCommunity(String name, String parentName) throws IllegalArgumentException {

        if(name.equals(parentName)) {
            throw new IllegalArgumentException("Community cannot have parent of same name");
        }

        if(this.getCommunity(name) != null) {
            throw new IllegalArgumentException("Community " + name + " already exists therefore cannot create");
        }

        String parentPath = getParentPathForCommunity(parentName);

        Community community = new Community(name, parentPath);

        return repository.save(community);
    }

    public Community moveCommunity(String name, String newParentName) {

        if(name.equals(newParentName)) {
            throw new IllegalArgumentException("Community cannot have parent of same name");
        }

        Community community = this.getCommunity(name);

        if(community == null) {
            throw new IllegalArgumentException("Community " + name + " does not exist therefore cannot move");
        }

        String newParentPath = getParentPathForCommunity(newParentName);

        community.setPath(newParentPath);

        return repository.save(community);
    }

    private String getParentPathForCommunity(String parentName) {

        String parentPath;
        if(parentName != null && parentName.length() > 0) {

            Community parentCommunity = this.getCommunity(parentName);

            if (parentCommunity == null) {
                throw new IllegalArgumentException("Cannot find parent community with name: " + parentName);
            } else {
                parentPath = new StringBuilder(parentCommunity.getPath()).append(parentCommunity.getName()).append(Community.PATH_SEPERATOR).toString();
            }
        } else {
            parentPath = Community.PATH_SEPERATOR;
        }
        return parentPath;
    }

    public CommunityTreeNode generateTree() {

        CommunityTreeNode rootNode = new CommunityTreeNode("root");

        List<Community> allCommunities = this.repository.findAllByOrderByPathAsc();

        logger.debug("All communities: {}", allCommunities);

        // First we construct the nodes
        final Map<String, CommunityTreeNode> allNodesByName = allCommunities.stream().map(community -> new CommunityTreeNode(community.getName())).collect(Collectors.toMap(CommunityTreeNode::getName, Function.identity()));

        logger.debug("All nodes by name: {}", allNodesByName);

        // Now we loop over all the nodes and update their parent and children
        allCommunities.forEach(community -> {

            CommunityTreeNode node =allNodesByName.get(community.getName());

            Matcher rootParentMatcher = ROOT_PATH_PATTERN.matcher(community.getPath());
            if(rootParentMatcher.matches()) {
                rootNode.getChildren().add(node);
            } else {

                logger.debug("Community path: {}", community.getPath());
                Matcher immediateParentMatcher = IMMEDIATE_PARENT_PATTERN.matcher(community.getPath());
                String extractedImmediateParent = null;
                while(immediateParentMatcher.find()) {
                    extractedImmediateParent = immediateParentMatcher.group(1);
                    logger.debug("Extracted parent: {}", extractedImmediateParent);
                }

                CommunityTreeNode parentNode = allNodesByName.get(extractedImmediateParent);

                parentNode.getChildren().add(node);

                logger.debug("ParentNode: {}", parentNode);
            }
        });

        return rootNode;

    }


}
