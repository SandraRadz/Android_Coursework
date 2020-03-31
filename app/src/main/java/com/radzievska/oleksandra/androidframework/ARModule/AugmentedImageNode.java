package com.radzievska.oleksandra.androidframework.ARModule;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.radzievska.oleksandra.androidframework.R;

import java.util.concurrent.CompletableFuture;

public class AugmentedImageNode extends AnchorNode {
    private static final String TAG = "AugmentedImageNode";

    // The augmented image represented by this node.
    private AugmentedImage image;
    private float maze_scale = 0.0f;
    private int resource = R.raw.andy;

    private CompletableFuture<ModelRenderable> mazeRenderable;

    public AugmentedImageNode(Context context) {
             mazeRenderable =
                ModelRenderable.builder()
                        .setSource(context,  resource)
                        .build();
    }

    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(AugmentedImage image) {
        this.image = image;
        // If any of the models are not loaded, then recurse when all are loaded.
        if (!mazeRenderable.isDone()) {
            CompletableFuture.allOf(mazeRenderable)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
            return;
        }

        // Set the anchor based on the center of the image.
        setAnchor(image.createAnchor(image.getCenterPose()));

        Node mazeNode = new Node();

//        final float maze_edge_size = 492.65f;
//        final float max_image_edge = Math.max(image.getExtentX(), image.getExtentZ());
//        maze_scale = max_image_edge / maze_edge_size;
//
//        // Scale Y an extra 10 times to lower the maze wall.
//        mazeNode.setLocalScale(new Vector3(maze_scale, maze_scale * 0.1f, maze_scale));

        mazeNode.setParent(this);
        mazeNode.setRenderable(mazeRenderable.getNow(null));
    }

    public AugmentedImage getImage() {
        return image;
    }

}
