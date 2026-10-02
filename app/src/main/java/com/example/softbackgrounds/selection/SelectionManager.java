package com.example.softbackgrounds.selection;

import com.example.softbackgrounds.SceneNode;

public class SelectionManager {

    public interface SelectionListener {
        void onSelectionChanged(SceneNode node);
    }

    private SceneNode selectedNode;
    private SelectionListener listener;

    public void setSelectionListener(SelectionListener listener) {
        this.listener = listener;
    }

    public void select(SceneNode node) {
        if (selectedNode == node) {
            return;
        }

        selectedNode = node;
        notifySelectionChanged();
    }

    public void deselect() {
        if (selectedNode == null) {
            return;
        }

        selectedNode = null;
        notifySelectionChanged();
    }

    public SceneNode getSelectedNode() {
        return selectedNode;
    }

    public boolean hasSelection() {
        return selectedNode != null;
    }

    private void notifySelectionChanged() {
        if (listener != null) {
            listener.onSelectionChanged(selectedNode);
        }
    }
}
