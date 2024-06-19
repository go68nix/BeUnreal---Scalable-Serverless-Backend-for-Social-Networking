package de.tum.cit.dos.eist.backend.functions;

import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import de.tum.cit.dos.eist.backend.infrastructure.BeUnrealRepository;
import de.tum.cit.dos.eist.backend.infrastructure.FakeAwsSns;
import de.tum.cit.dos.eist.backend.infrastructure.FileStorage;
import de.tum.cit.dos.eist.backend.models.User;

public class BeUnrealTimeFunction
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private BeUnrealRepository repository;
    private FileStorage fileStorage;
    private FakeAwsSns awsSns;

    public BeUnrealTimeFunction() {
        repository = new BeUnrealRepository();
        fileStorage = new FileStorage();
        awsSns = new FakeAwsSns();

    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        //TODO: Part 2 send a push notification to each user with the message "Time to BeUnreal."
        // Use the deleteImage method to delete the images
        repository.getAllUsers().forEach(x-> awsSns.sendPushNotification(x,"Time to BeUnreal."));
        repository.getAllUsers().forEach(x-> {
            repository.updateHasPostedToday(x.uid(),false);
        });
        repository.getAllUsers().forEach(x->deleteImage(x.uid(),FileStorage.IMAGES_BUCKET));
        return new APIGatewayProxyResponseEvent().withStatusCode(200);
    }

    private void deleteImage(String userId, String folderName) {
        fileStorage.deleteFile(FileStorage.BLURRED_IMAGES_FOLDER+"/"+userId+".jpg");
        fileStorage.deleteFile(FileStorage.UNBLURRED_IMAGES_FOLDER+"/"+userId+".jpg");
        
        //TODO: Part 2 create a key to delete the blurred and unblurred images
    }
}