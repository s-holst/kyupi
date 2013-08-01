/* Generated By:JavaCC: Do not edit this line. StilConstants.java */
package org.kyupi.data.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface StilConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int COMMENT = 5;
  /** RegularExpression Id. */
  int STIL = 6;
  /** RegularExpression Id. */
  int HEADER = 7;
  /** RegularExpression Id. */
  int SIGNALS = 8;
  /** RegularExpression Id. */
  int SIGNALGROUPS = 9;
  /** RegularExpression Id. */
  int TIMING = 10;
  /** RegularExpression Id. */
  int SCANSTRUCTURES = 11;
  /** RegularExpression Id. */
  int PATTERNBURST = 12;
  /** RegularExpression Id. */
  int PATTERNEXEC = 13;
  /** RegularExpression Id. */
  int PROCEDURES = 14;
  /** RegularExpression Id. */
  int MACRODEFS = 15;
  /** RegularExpression Id. */
  int PATTERN = 16;
  /** RegularExpression Id. */
  int SCANCHAIN = 17;
  /** RegularExpression Id. */
  int SCANLENGTH = 18;
  /** RegularExpression Id. */
  int SCANIN = 19;
  /** RegularExpression Id. */
  int SCANOUT = 20;
  /** RegularExpression Id. */
  int SCANINVERSION = 21;
  /** RegularExpression Id. */
  int SCANCELLS = 22;
  /** RegularExpression Id. */
  int SCANMASTERCLOCK = 23;
  /** RegularExpression Id. */
  int CALL = 24;
  /** RegularExpression Id. */
  int digit = 25;
  /** RegularExpression Id. */
  int letter = 26;
  /** RegularExpression Id. */
  int number_float = 27;
  /** RegularExpression Id. */
  int number_integer = 28;
  /** RegularExpression Id. */
  int id_head = 29;
  /** RegularExpression Id. */
  int id_tail = 30;
  /** RegularExpression Id. */
  int identifier = 31;
  /** RegularExpression Id. */
  int equals = 32;
  /** RegularExpression Id. */
  int quote = 33;
  /** RegularExpression Id. */
  int plus = 34;
  /** RegularExpression Id. */
  int semicolon = 35;
  /** RegularExpression Id. */
  int bopen = 36;
  /** RegularExpression Id. */
  int bclose = 37;
  /** RegularExpression Id. */
  int nonbrace_character = 38;
  /** RegularExpression Id. */
  int string = 39;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\t\"",
    "<COMMENT>",
    "\"STIL\"",
    "\"Header\"",
    "\"Signals\"",
    "\"SignalGroups\"",
    "\"Timing\"",
    "\"ScanStructures\"",
    "\"PatternBurst\"",
    "\"PatternExec\"",
    "\"Procedures\"",
    "\"MacroDefs\"",
    "\"Pattern\"",
    "\"ScanChain\"",
    "\"ScanLength\"",
    "\"ScanIn\"",
    "\"ScanOut\"",
    "\"ScanInversion\"",
    "\"ScanCells\"",
    "\"ScanMasterClock\"",
    "\"Call\"",
    "<digit>",
    "<letter>",
    "<number_float>",
    "<number_integer>",
    "<id_head>",
    "<id_tail>",
    "<identifier>",
    "\"=\"",
    "\"\\\'\"",
    "\"+\"",
    "\";\"",
    "\"{\"",
    "\"}\"",
    "<nonbrace_character>",
    "<string>",
  };

}
