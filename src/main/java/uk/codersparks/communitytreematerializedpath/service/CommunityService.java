package uk.codersparks.communitytreematerializedpath.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.codersparks.communitytreematerializedpath.model.Community;
import uk.codersparks.communitytreematerializedpath.model.CommunityTreeNode;
import uk.codersparks.communitytreematerializedpath.repository.CommunityRepository;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Service
public class CommunityService {

    private static final Logger logger = LoggerFactory.getLogger(CommunityService.class);

    private static final Pattern ROOT_PATH_PATTERN = Pattern.compile("^" + Community.PATH_SEPERATOR + "$");
    private static final Pattern IMMEDIATE_PARENT_PATTERN = Pattern.compile(Community.PATH_SEPERATOR + "([^" + Community.PATH_SEPERATOR + "]+)" + Community.PATH_SEPERATOR + "$");

    private final CommunityRepository repository;

    public CommunityService(CommunityRepository repository) {
        this.repository = repository;
    }


    public Community getCommunity(String name) {
        return this.repository.findOne(name);
    }

    public List<Community> getAllCommunities() {
        logger.info("Getting all communities");
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

        String originalFullPathToCommunity = community.getIdentityPath();

        String newParentPath;

        if(newParentName.equals("")) {
            newParentPath = "/";
        } else {
            newParentPath = getParentPathForCommunity(newParentName);
        }

        community.setPath(newParentPath);

        String newFullPathToCommunity = community.getIdentityPath();

        // Now we have to update any sub tree that has community as a parent
        // Build a list of those communities in the sub tree
        List<Community> subCommunities = getAllCommunities().stream().filter(c ->c.getPath().startsWith(originalFullPathToCommunity)).collect(Collectors.toList());

        logger.info("Sub communities of {}: {}", community, subCommunities);

        if(subCommunities.size() > 0) {
            subCommunities.forEach(c -> {

                c.setPath(c.getPath().replace(originalFullPathToCommunity, newFullPathToCommunity));
            });

            repository.save(subCommunities);
        }
        Community returnValue = repository.save(community);

        return returnValue;
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

    public List<CommunityTreeNode> generateTree() {

        List<CommunityTreeNode> rootCommunityTreeNodes = new ArrayList<>();

        List<Community> allCommunities = this.repository.findAllByOrderByPathAsc();

        logger.debug("All communities: {}", allCommunities);

        // First we construct the nodes
        final Map<String, CommunityTreeNode> allNodesByName = allCommunities.stream().map(community -> new CommunityTreeNode(community.getName(), community.getPath())).collect(Collectors.toMap(CommunityTreeNode::getName, Function.identity()));

        logger.debug("All nodes by name: {}", allNodesByName);

        // Now we loop over all the nodes and update their parent and children
        allCommunities.forEach(community -> {

            CommunityTreeNode node =allNodesByName.get(community.getName());

            Matcher rootParentMatcher = ROOT_PATH_PATTERN.matcher(community.getPath());
            if(rootParentMatcher.matches()) {
                rootCommunityTreeNodes.add(node);
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

        return rootCommunityTreeNodes;

    }

    public int distanceToCommonAncestor(String from, String to) {

        logger.info("Calculating distance to common ancestor from {} to {}", from, to);

        Community fromCommunity = repository.findOne(from);

        if (fromCommunity == null) {
            throw new IllegalArgumentException("Cannont find community with name: " + from);
        }

        Community toCommunity = repository.findOne(to);

        if (toCommunity == null) {
            throw new IllegalArgumentException("Cannot find to community with name: " + to);
        }

        return distanceToCommonAncestor(fromCommunity, toCommunity);

    }

    public int distanceToCommonAncestor(Community from, Community to) {

        // Simple case when communities equal
        if(from.equals(to)) {
            return 0;
        }

        String[] fromPath = from.getPath().split(Community.PATH_SEPERATOR);

        logger.debug("From Path: {}", Arrays.asList(fromPath));

        List<String> toPath = asList(to.getPath().split(Community.PATH_SEPERATOR));

        logger.debug("To Path: {}", toPath);

        int ancestorCount = 0;

        String parent = null;
        // The distance of the common ancestor
        for(int i = fromPath.length - 1; i >= 0; i--) {
            ancestorCount++;
            parent = fromPath[i];
            if(toPath.contains(parent)) {
                logger.debug("Found ancestor parent: {}", parent);
                break;
            }
        }

        logger.info("Distance to common ancestor: {}", ancestorCount);

        return ancestorCount;

    }


}
