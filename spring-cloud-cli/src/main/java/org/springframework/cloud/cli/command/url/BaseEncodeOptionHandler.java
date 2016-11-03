package org.springframework.cloud.cli.command.url;

import static java.util.Arrays.asList;

import joptsimple.OptionSpec;
import org.springframework.boot.cli.command.options.OptionHandler;


public class BaseEncodeOptionHandler extends OptionHandler {
	OptionSpec<String> charsetOption;

	@Override
	protected final void options() {
		this.charsetOption = option(asList("charset", "c"),
				"Character set (defaults to UTF-8)").withRequiredArg();
		doOptions();
	}

	protected void doOptions(){
		
	}
}
