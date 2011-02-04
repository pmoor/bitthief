/*
 * BitThief - A Free Riding BitTorrent Client
 * Copyright (C) 2006 Patrick Moor <patrick@moor.ws>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */

package ws.moor.bt.grapher;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO(pmoor): Javadoc
 */
public class Grapher {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Please specify a tab-separated values file");
      System.exit(1);
    }
    File file = new File(args[0]);
    final CSVMapCollector collector = new CSVMapCollector(new CSVSkipFilter(new CSVInputStream(new FileInputStream(file)), 0 * 1000));

    JFrame window = new JFrame("Grapher");
    window.setSize(1100, 800);
    window.setLayout(new BorderLayout());
    final ChartPanel chartPanel = new ChartPanel(null);

    List<String> possibleNames = collector.getAvailableStreams();
    Collections.sort(possibleNames);
    TreeNode root = convertToTree(possibleNames);

    final JTree tree = new JTree(root);
    tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        List<String> names = new ArrayList<String>();
        final TreePath[] paths = tree.getSelectionModel().getSelectionPaths();
        if (paths == null) {
          chartPanel.setChart(null);
          return;
        }
        for (TreePath path : paths) {
          Object lastPath = path.getLastPathComponent();
          if (lastPath instanceof DefaultMutableTreeNode) {
            Object value = ((DefaultMutableTreeNode) lastPath).getUserObject();
            if (value instanceof NodeValue) {
              names.add(value.toString());
            }
          }
        }
        chartPanel.setChart(createChart(collector, names.toArray(new String[names.size()])));
      }
    });
    Font font = tree.getFont();
    tree.setFont(font.deriveFont(10.0f));
    JScrollPane scrollPane = new JScrollPane(tree);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(scrollPane);
    splitPane.setRightComponent(chartPanel);
    splitPane.setDividerLocation(200);

    window.setContentPane(splitPane);
    window.setVisible(true);
  }

  private static JFreeChart createChart(CSVMapCollector collector, String[] names) {
    ChartBuilder builder = new ChartBuilder(null);
    builder.showLegend(names.length < 10);
    for (String name : names) {
      builder.addDataset(name, collector.getStream(name));
    }
    return builder.getChart();
  }

  private static TreeNode convertToTree(List<String> names) {
    Map<String, DefaultMutableTreeNode> existingNodes =
        new HashMap<String, DefaultMutableTreeNode>();

    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Counters");

    for (String name : names) {
      DefaultMutableTreeNode bestFittingParent = rootNode;
      String[] parts = name.split("\\.|@");
      StringBuilder prefix = new StringBuilder();
      for (int i = 0; i < parts.length - 1; i++) {
        prefix.append(parts[i]);
        DefaultMutableTreeNode parent = existingNodes.get(prefix.toString());
        if (parent != null) {
          bestFittingParent = parent;
        } else {
          DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(parts[i]);
          bestFittingParent.add(newNode);
          existingNodes.put(prefix.toString(), newNode);
          bestFittingParent = newNode;
        }
      }

      DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(new NodeValue(name));
      bestFittingParent.add(currentNode);
      currentNode.setAllowsChildren(false);
    }
    return rootNode;
  }

  private static class NodeValue {

    private final String fullName;

    public NodeValue(String fullName) {
      this.fullName = fullName;
    }

    public String toString() {
      return fullName;
    }
  }
}
