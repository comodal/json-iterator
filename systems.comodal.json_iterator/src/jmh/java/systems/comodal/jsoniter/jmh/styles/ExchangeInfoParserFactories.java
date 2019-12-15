package systems.comodal.jsoniter.jmh.styles;

import systems.comodal.jsoniter.factory.JsonIterParserFactory;
import systems.comodal.jsoniter.jmh.data.exchange.ExchangeInfo;

public final class ExchangeInfoParserFactories {

  private static final IocLoopCharSwitch IOC_LOOP_CHAR_SWITCH_INSTANCE = new IocLoopCharSwitch();
  private static final IocLoopCharIf IOC_LOOP_CHAR_IF_INSTANCE = new IocLoopCharIf();
  private static final IocLoopCompareStringFieldToCharsIf IOC_LOOP_COMPARE_STRING_FIELD_TO_CHARS_IF_INSTANCE = new IocLoopCompareStringFieldToCharsIf();
  private static final IocLoopCompareStringFieldToCharsIfMask IOC_LOOP_COMPARE_STRING_FIELD_TO_CHARS_IF__MASK_INSTANCE = new IocLoopCompareStringFieldToCharsIfMask();
  private static final LoopStringIf LOOP_STRING_IF_INSTANCE = new LoopStringIf();
  private static final LoopStringSwitch LOOP_STRING_SWITCH_INSTANCE = new LoopStringSwitch();
  private static final StaticFieldOrdering STATIC_FIELD_ORDERING_INSTANCE = new StaticFieldOrdering();
  private static final CompactStaticFieldOrdering COMPACT_STATIC_FIELD_ORDERING_INSTANCE = new CompactStaticFieldOrdering();

  public static final class IocLoopCharSwitchFactoryExchangeInfo implements JsonIterParserFactory<ExchangeInfo> {

    @Override
    public IocLoopCharSwitch create(final Class<ExchangeInfo> parserType) {
      return IOC_LOOP_CHAR_SWITCH_INSTANCE;
    }
  }

  public static final class IocLoopCharIfFactoryExchangeInfo implements JsonIterParserFactory<ExchangeInfo> {

    @Override
    public IocLoopCharIf create(final Class<ExchangeInfo> parserType) {
      return IOC_LOOP_CHAR_IF_INSTANCE;
    }
  }

  public static final class IocLoopCompareStringFieldToCharsIfExchangeInfoFactory implements JsonIterParserFactory<ExchangeInfo> {

    @Override
    public IocLoopCompareStringFieldToCharsIf create(final Class<ExchangeInfo> parserType) {
      return IOC_LOOP_COMPARE_STRING_FIELD_TO_CHARS_IF_INSTANCE;
    }
  }

  public static final class IocLoopCompareStringFieldToCharsIfMaskExchangeInfoFactory implements JsonIterParserFactory<ExchangeInfo> {

    @Override
    public IocLoopCompareStringFieldToCharsIfMask create(final Class<ExchangeInfo> parserType) {
      return IOC_LOOP_COMPARE_STRING_FIELD_TO_CHARS_IF__MASK_INSTANCE;
    }
  }

  public static final class LoopStringIfExchangeInfoFactory implements JsonIterParserFactory<ExchangeInfo> {

    @Override
    public LoopStringIf create(final Class<ExchangeInfo> parserType) {
      return LOOP_STRING_IF_INSTANCE;
    }
  }

  public static final class LoopStringSwitchExchangeInfoFactory implements JsonIterParserFactory<ExchangeInfo> {

    @Override
    public LoopStringSwitch create(final Class<ExchangeInfo> parserType) {
      return LOOP_STRING_SWITCH_INSTANCE;
    }
  }

  public static final class StaticFieldOrderingFactoryExchangeInfo implements JsonIterParserFactory<ExchangeInfo> {

    @Override
    public StaticFieldOrdering create(final Class<ExchangeInfo> parserType) {
      return STATIC_FIELD_ORDERING_INSTANCE;
    }
  }

  public static final class CompactStaticFieldOrderingFactoryExchangeInfo implements JsonIterParserFactory<ExchangeInfo> {

    @Override
    public CompactStaticFieldOrdering create(final Class<ExchangeInfo> parserType) {
      return COMPACT_STATIC_FIELD_ORDERING_INSTANCE;
    }
  }
}
