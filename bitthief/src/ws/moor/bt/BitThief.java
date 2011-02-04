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

package ws.moor.bt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import ws.moor.bt.downloader.TorrentDownload;
import ws.moor.bt.downloader.TorrentDownloadConfiguration;
import ws.moor.bt.gui.BitThiefGUI;
import ws.moor.bt.torrent.MetaInfo;
import ws.moor.bt.torrent.MetaInfoBuilder;
import ws.moor.bt.util.LoggingUtil;
import ws.moor.bt.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class BitThief {

  private static final String METAFILE_OPTION = "m";
  private static final String OUTPUTDIR_OPTION = "o";
  private static final String PORT_OPTION = "p";
  private static final String GUI_OPTION = "g";
  private static final String NOSEEDS_OPTION = "n";
  private static final String NOINITIATE_OPTION = "i";
  private static final String HONESTBITFIELD_OPTION = "b";
  private static final String ANNOUNCEINTERVAL_OPTION = "a";
  private static final String SHARERATIO_OPTION = "s";
  private static final String QUITONFINISH_OPTION = "q";
  private static final String BITFIELDPERCENT_OPTION = "f";
  private static final String DOUPLOAD_OPTION = "u";
  private static final String UPLOADREAL_OPTION = "r";
  private static final String HELP_OPTION = "h";
  private static final String MAXUPLOAD_OPTION = "uploadrate";
  private static final String UPLOADSLOTS_OPTION = "uploadslots";
  private static final String DENYPERCENTAGE_OPTION = "denypercentage";

  private static final Logger logger = LoggingUtil.getLogger(BitThief.class);

  public static void main(String[] arguments) throws Exception {
    BitThiefConfiguration bitThiefConfiguration = loadBitThiefConfiguration();
    configureStaticStuff(bitThiefConfiguration);

    CommandLine line = parseCommandLine(arguments);

    logger.info("BitThief " + Version.getLongVersionString());
    logArguments(arguments);

    logger.info("setting up environment");
    Environment environment = new Environment(bitThiefConfiguration);

    boolean showGui = true;
    if (line.hasOption(METAFILE_OPTION) && line.hasOption(OUTPUTDIR_OPTION)) {
      File torrentFile = new File(line.getOptionValue(METAFILE_OPTION));
      File targetDirectory = new File(line.getOptionValue(OUTPUTDIR_OPTION));

      logger.info("reading meta info file " + torrentFile);
      MetaInfo metaInfo = MetaInfoBuilder.fromFile(torrentFile);

      TorrentDownloadConfiguration configuration =
          new TorrentDownloadConfiguration(metaInfo, targetDirectory);
      initConfiguration(configuration, line);

      logger.info("creating torrent download");
      TorrentDownload download = new TorrentDownload(environment, configuration);

      logger.info("starting torrent download");
      download.start();

      showGui = configuration.showGui();
    }

    if (showGui) {
      logger.info("starting GUI");
      BitThiefGUI gui = new BitThiefGUI(environment);
      gui.show();
    }
  }

  private static void configureStaticStuff(BitThiefConfiguration configuration) throws IOException {
    InputStream stream = ClassLoader.getSystemResourceAsStream(configuration.getLoggingPropertyFile());
    if (stream != null) {
      Properties properties = new Properties();
      properties.load(stream);
      PropertyConfigurator.configure(properties);
    } else {
      System.err.println("unable to load logging property file");
      System.exit(1);
    }
  }

  private static BitThiefConfiguration loadBitThiefConfiguration() {
    BitThiefConfiguration configuration = BitThiefConfiguration.fromPropertiesFile();
    if (configuration == null) {
      System.err.println("Error loading property file.");
      System.exit(1);
    }
    return configuration;
  }

  private static void logArguments(String[] arguments) {
    StringBuilder commandLine = new StringBuilder();
    commandLine.append("Arguments: ");
    commandLine.append(StringUtil.join(Arrays.asList(arguments), " "));
    logger.info(commandLine.toString());
  }

  private static void initConfiguration(TorrentDownloadConfiguration configuration, CommandLine line) {
    if (line.hasOption(PORT_OPTION)) {
      configuration.setListeningPort(
          Integer.parseInt(line.getOptionValue(PORT_OPTION)));
    }
    if (line.hasOption(ANNOUNCEINTERVAL_OPTION)) {
      configuration.setInitialAnnounceInterval(
          Integer.parseInt(line.getOptionValue(ANNOUNCEINTERVAL_OPTION)));
    }
    if (line.hasOption(SHARERATIO_OPTION)) {
      configuration.setShareRatio(
          Double.parseDouble(line.getOptionValue(SHARERATIO_OPTION)));
    }
    if (line.hasOption(MAXUPLOAD_OPTION)) {
      configuration.setMaxUploadRate(
          Integer.parseInt(line.getOptionValue(MAXUPLOAD_OPTION)));
    }
    if (line.hasOption(BITFIELDPERCENT_OPTION)) {
      configuration.setBitfieldPercent(
          Integer.parseInt(line.getOptionValue(BITFIELDPERCENT_OPTION)));
    }
    if (line.hasOption(UPLOADSLOTS_OPTION)) {
      configuration.setUploadSlots(
          Integer.parseInt(line.getOptionValue(UPLOADSLOTS_OPTION)));
    }
    if (line.hasOption(DENYPERCENTAGE_OPTION)) {
      configuration.setPieceUploadDenyPercentage(
          Integer.parseInt(line.getOptionValue(DENYPERCENTAGE_OPTION)));
    }
    configuration.setShowGui(line.hasOption(GUI_OPTION));
    configuration.setDownloadFromSeeders(!line.hasOption(NOSEEDS_OPTION));
    configuration.setInitiateConnections(!line.hasOption(NOINITIATE_OPTION));
    configuration.setSendRealBitField(line.hasOption(HONESTBITFIELD_OPTION));
    configuration.setQuitOnFinish(line.hasOption(QUITONFINISH_OPTION));
    configuration.setUploadRealData(line.hasOption(UPLOADREAL_OPTION));
    configuration.setUploading(line.hasOption(DOUPLOAD_OPTION));
  }

  private static CommandLine parseCommandLine(String[] args) {
    CommandLineParser parser = new GnuParser();
    Options options = createOptions();

    try {
      CommandLine commandLine = parser.parse(options, args, true);
      if (commandLine.hasOption(HELP_OPTION)) {
        printHelpTextAndExit(options);
      }
      return commandLine;
    } catch (ParseException e) {
      printHelpTextAndExit(options);
    }
    return null;
  }

  private static void printHelpTextAndExit(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("BitThief", options, true);
    System.exit(1);
  }

  private static Options createOptions() {
    Options options = new Options();
    options.addOption(OptionBuilder.
        withLongOpt("metafile").
        withDescription("meta file").
        withArgName("file").
        hasArg().
        create(METAFILE_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("output").
        withDescription("output directory").
        withArgName("directory").
        hasArg().
        create(OUTPUTDIR_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("port").
        withDescription("listening port (default: " +
            TorrentDownloadConfiguration.DEFAULT_LISTENING_PORT + ")").
        withArgName("port").
        hasArg().
        create(PORT_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("gui").
        withDescription("show gui").
        create(GUI_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("noseeds").
        withDescription("do not leech from seeds").
        create(NOSEEDS_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("noinitiate").
        withDescription("do not initiate outgoing connections").
        create(NOINITIATE_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("honestbitfield").
        withDescription("send real bit field (default: send empty field)").
        create(HONESTBITFIELD_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("bitfieldpercent").
        withDescription("percent of pieces to set in bitfield (default: " +
            TorrentDownloadConfiguration.DEFAULT_BITFIELD_PERCENT + ")").
        withArgName("%").
        hasArg().
        create(BITFIELDPERCENT_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("announceinterval").
        withDescription("initial announce interval (default: " +
            TorrentDownloadConfiguration.DEFAULT_ANNOUNCE_INTERVAL + ")").
        withArgName("seconds").
        hasArg().
        create(ANNOUNCEINTERVAL_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("shareratio").
        withDescription("share ratio to fake (default: " +
            TorrentDownloadConfiguration.DEFAULT_SHARE_RATIO + ")").
        withArgName("ratio").
        hasArg().
        create(SHARERATIO_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("quitonfinish").
        withDescription("quit on finish").
        create(QUITONFINISH_OPTION));
    options.addOption(OptionBuilder.
        withDescription("max data upload rate (default: " +
            TorrentDownloadConfiguration.DEFAULT_MAX_UPLOAD_RATE + ")").
        withArgName("KB/s").
        hasArg().
        create(MAXUPLOAD_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("uploadreal").
        withDescription("do upload real data").
        create(UPLOADREAL_OPTION));
    options.addOption(OptionBuilder.
        withDescription("number of upload slots (default: " +
            TorrentDownloadConfiguration.DEFAULT_UPLOAD_SLOTS + ")").
        withArgName("slots").
        hasArg().
        create(UPLOADSLOTS_OPTION));
    options.addOption(OptionBuilder.
        withLongOpt("upload").
        withDescription("do upload (default: " +
            TorrentDownloadConfiguration.DEFAULT_UPLOADING_STATUS + ")").
        create(DOUPLOAD_OPTION));
    options.addOption(OptionBuilder.
        withDescription("percentage of pieces to deny for uploading (default: " +
            TorrentDownloadConfiguration.DEFAULT_PIECE_UPLOAD_DENY_PERCENTAGE + ")").
        withArgName("%").
        hasArg().
        create(DENYPERCENTAGE_OPTION));
    options.addOption(OptionBuilder.
        withDescription("display this help text").
        withLongOpt("help").
        create(HELP_OPTION));
    return options;
  }
}
