package systems.comodal.jsoniter.generate;

import java.util.ArrayList;
import java.util.List;

public class JIParserConfigVal implements JIParserConfig {

  private final boolean skipUnexpectedFields;
  private final JIParserStyle style;
  private String tab;
  private final List<JIParser> parsers;

  private JIParserConfigVal(final boolean skipUnexpectedFields,
                            final JIParserStyle style,
                            final int tabSize) {
    this.skipUnexpectedFields = skipUnexpectedFields;
    this.style = style;
    this.tab = " ".repeat(tabSize);
    this.parsers = new ArrayList<>();
  }

  @Override
  public boolean skipUnexpectedFields() {
    return skipUnexpectedFields;
  }

  @Override
  public JIParser createParser(final JIParserGenerator generator) {
    final var parser = style.createParser(this, generator);
    parsers.add(parser);
    return parser;
  }

  @Override
  public void printParsers() {
    for (final var parser : parsers) {
      System.out.println(parser.getCode());
    }
  }

  @Override
  public String getTab() {
    return tab;
  }

  @Override
  public String toString() {
    return "JIParserConfigVal{" +
        "skipUnexpectedFields=" + skipUnexpectedFields +
        ", style=" + style +
        ", tabSize=" + tab.length() +
        '}';
  }

  static final class JIParserConfigBuilder implements JIParserConfig.Builder {

    private boolean skipUnexpectedFields;
    private JIParserStyle style;
    private int tabSize = 2;

    JIParserConfigBuilder() {
    }

    @Override
    public JIParserConfig create() {
      return new JIParserConfigVal(skipUnexpectedFields, style, tabSize);
    }

    @Override
    public Builder skipUnexpectedFields(final boolean skipUnexpectedFields) {
      this.skipUnexpectedFields = skipUnexpectedFields;
      return this;
    }

    @Override
    public Builder style(final JIParserStyle style) {
      this.style = style;
      return this;
    }

    @Override
    public Builder tabSize(final int tabSize) {
      this.tabSize = tabSize;
      return this;
    }
  }
}
