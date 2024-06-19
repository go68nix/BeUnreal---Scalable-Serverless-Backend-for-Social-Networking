package de.tum.cit.dos.eist.backend.functions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import de.tum.cit.dos.eist.backend.infrastructure.BeUnrealRepository;
import de.tum.cit.dos.eist.backend.infrastructure.FileStorage;
import de.tum.cit.dos.eist.backend.models.GetFeedResponse;
import de.tum.cit.dos.eist.backend.models.Post;
import de.tum.cit.dos.eist.backend.models.User;

public class GetFeedFunction implements RequestHandler<APIGatewayProxyRequestEvent, GetFeedResponse> {
    private BeUnrealRepository repository;
    private FileStorage fileStorage;

    public GetFeedFunction() {
        repository = new BeUnrealRepository();
        fileStorage = new FileStorage();
    }

    public GetFeedResponse handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        // First, we need read the userId from the request. This is the user
        // that calls the function. We ignore authentication for now.
        String userId = request.getQueryStringParameters().get("userId");

        // Then, we need to get the user from the repository to check if the
        // user has posted today.
        User user = repository.getUser(userId);

        // After getting the user status, we can get the posts for the feed.
        List<Post> posts = getPosts(user);

        // The posts need to be sorted before returning them.
        sortFeed(user.uid(), posts);

        // Finally, we return the posts and the hasPostedToday flag. The
        // frontend will use this flag to decide if the user can post again.
        return new GetFeedResponse(posts, user.hasPostedToday());
    }

    private List<Post> getPosts(User requester) {

        List<Post> posts = new ArrayList<>();


        /*When creating a post, we need a presigned URL.
        Incorporate the following code with the correct parameters for that.
         */
        // The image path depends on whether the user has posted or not.


        // We need to generate a presigned URL for the image so that the
        // frontend can access the image.

        if(requester.hasPostedToday()){
            String key = getImagePath(requester.uid(), requester.hasPostedToday());
            String presignedUrl = fileStorage.generatePresignedUrl(FileStorage.IMAGES_BUCKET, key);
            posts.add(new Post(requester.uid(),requester.displayName(),presignedUrl));
        }
        if(!repository.getFriends(requester.uid()).isEmpty()){

            repository.getFriends(requester.uid()).stream().filter(User::hasPostedToday).
                    forEach(x->posts.add(new Post(x.uid(),x.displayName(),fileStorage.
                            generatePresignedUrl(FileStorage.IMAGES_BUCKET, getImagePath(x.uid(), requester.hasPostedToday())))));

        }


        //TODO: Part 1 your code here
        return posts;
    }

    private void sortFeed(String userId, List<Post> posts) {
        if(1<posts.size()){
            Post firstPost = posts.get(0);

            List<Post> sublist = new ArrayList<>(posts.subList(1, posts.size()));
            sublist.sort(Comparator.comparing(Post::displayName));


            posts.clear();
            posts.add(firstPost);
            posts.addAll(sublist);
        }


        //TODO: Part 1 Sort the list posts
    }

    private String getImagePath(String userId, boolean hasUserPosted) {
        // Decide which image folder to use from FileStorage
        String folderName ;
        if(!hasUserPosted){
            folderName =fileStorage.BLURRED_IMAGES_FOLDER+"/"+ userId + ".jpg";
        }
        else{
            folderName =fileStorage.UNBLURRED_IMAGES_FOLDER+"/"+ userId + ".jpg";
        }
        //TODO: Part 1 your code
        return folderName;
    }
}
