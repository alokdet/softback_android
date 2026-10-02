package com.example.softbackgrounds.interaction;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.softbackgrounds.SceneNode;

public class InteractionEngine {
    private static final String TAG = "SoftLauncherInteraction";
    private final Context context;

    public InteractionEngine(Context context) {
        this.context = context.getApplicationContext();
    }

    public void handleTap(SceneNode node) {
        if (node == null) {
            return;
        }

        /*
         * Temporary behavior.
         *
         * Later this information will come
         * directly from the SLDF interaction
         * definition.
         */
        InteractionAction action = new InteractionAction(InteractionAction.Type.LOG, node.name);
        execute(action);
    }

    public void moveNode(SceneNode node, float deltaX, float deltaY) {
        if (node == null) {
            return;
        }

        node.x += deltaX;
        node.y += deltaY;
    }

    private void execute(InteractionAction action) {
        switch (action.getType()) {
            case LOG:
                Toast.makeText(context, "Action executed: " + action.getTarget(), Toast.LENGTH_SHORT).show();
                break;

            case OPEN_APP:
                // Will be implemented later.
                break;

            case NAVIGATE:
                // Will be implemented later.
                break;

            case ANIMATE:
                // Animation engine will handle this.
                break;

            case NONE:
            default:
                break;
        }
    }
}
