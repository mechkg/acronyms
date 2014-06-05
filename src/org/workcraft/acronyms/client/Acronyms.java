package org.workcraft.acronyms.client;

import java.util.Arrays;
import java.util.Comparator;

import org.workcraft.acronyms.shared.AcronymGeneratorResult;
import org.workcraft.acronyms.shared.AcronymGeneratorResult2;
import org.workcraft.acronyms.shared.SharedUtil;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class Acronyms implements EntryPoint {
	private AcronymGeneratorAsync generatorService = AcronymGenerator.Util
			.getInstance();

	final InlineLabel label = new InlineLabel("Project title: ");
	final TextBox input = new TextBox();
	final Button goButton = new Button("Go!");
	final ListBox lengthSelection = new ListBox();
	final ListBox modeSelection = new ListBox();
	final CheckBox allowSkipWords = new CheckBox();
	final FlowPanel settingsPanel = new FlowPanel();
	final HTMLPanel modeSettingsPanel = new HTMLPanel("span", "");
	final PopupPanel loadingPanel = new PopupPanel();

	private void generate() {
		final String in = input.getText();
		generatorService.generate(in, allowSkipWords.getValue(),
				new AsyncCallback<AcronymGeneratorResult>() {
					@Override
					public void onSuccess(AcronymGeneratorResult result) {
						showMessage("Hover the mouse cursor over acronyms to see how they were built");
						RootPanel outputDiv = RootPanel.get("output");
						outputDiv.clear();

						final String[] words = in.split("\\s+");
						
						Arrays.sort (result.acronyms, new Comparator<short[]>() {
							@Override
							public int compare(short[] o1, short[] o2) {
								String a1 = SharedUtil.mkString(o1, words).toUpperCase();
								String a2 = SharedUtil.mkString(o2, words).toUpperCase();
								
								if (a1.charAt(0) == words[0].charAt(0)) {
									if (a2.charAt(0) == words[0].charAt(0))
										return a1.compareTo(a2);
									else return -1;
								} else if (a2.charAt(0) == words[0].charAt(0)) {
									return 1;
								} else 
									return a1.compareTo(a2);
							}
						});

						for (short[] r : result.acronyms) {
							final Label expLabel = new Label(SharedUtil.mkString(r, words).toUpperCase());

							SafeHtmlBuilder explanation = new SafeHtmlBuilder();
							
							for (int i = 0; i < words.length; i++) {
								if (i != 0) explanation.appendHtmlConstant(" ");
								if (r[i] == -1) {
									explanation.appendEscaped(words[i]);
								} else {
									explanation.appendEscaped(words[i].substring(0, r[i]));
									explanation.appendHtmlConstant("<span class=\"highlight\">");
									explanation.appendEscaped(words[i].substring(r[i], r[i]+1).toUpperCase());
									explanation.appendHtmlConstant("</span>");
									if ((r[i]+1) < words[i].length())
										explanation.appendEscaped(words[i].substring(r[i]+1));
								}
							}

							mkExplanationPopup(explanation.toSafeHtml(), expLabel);
							outputDiv.add(expLabel);
						}
						
						if (result.acronyms.length == 0)
							showMessage("No acronyms found :( Try a longer title or allow skipping words!");
						
						loadingPanel.hide();
					}

					@Override
					public void onFailure(Throwable caught) {
						showErrorMessage("Something has broken on the server :( Try again!");
						loadingPanel.hide();
					}
				});

	}

	private void clearMessage() {
		RootPanel messageDiv = RootPanel.get("messageContainer");
		messageDiv.clear();
	}

	private void showErrorMessage(String messageText) {
		RootPanel messageDiv = RootPanel.get("messageContainer");
		messageDiv.clear();
		Label message = new Label(messageText);
		message.addStyleName("message");
		message.addStyleName("red");
		messageDiv.add(message);
	}

	private void showMessage(String messageText) {
		RootPanel messageDiv = RootPanel.get("messageContainer");
		messageDiv.clear();
		Label message = new Label(messageText);
		message.addStyleName("message");
		messageDiv.add(message);
	}

	private void mkExplanationPopup(final SafeHtml explanation,
			final Label relative) {
		HTMLPanel htmlp = new HTMLPanel(explanation);
		htmlp.addStyleName("explanation");

		final PopupPanel popup = new PopupPanel();

		popup.setWidget(htmlp);
		popup.setAutoHideEnabled(true);

		relative.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				popup.showRelativeTo(relative);
			}
		});

		relative.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				popup.hide();
			}
		});
	}

	private void generateSubseq() {
		final String in = input.getText();
		final int selectedLength = lengthSelection.getSelectedIndex() + 4;

		generatorService.generateSubseq(in, selectedLength,
				new AsyncCallback<AcronymGeneratorResult2>() {
					@Override
					public void onFailure(Throwable caught) {
						showErrorMessage("Something has broken on the server :( Try again!");
						loadingPanel.hide();
					}

					@Override
					public void onSuccess(AcronymGeneratorResult2 result) {
						RootPanel outputDiv = RootPanel.get("output");
						outputDiv.clear();
						showMessage("Hover the mouse cursor over acronyms to see how they were built");

						for (short[] r : result.acronyms) {
							final Label expLabel = new Label(SharedUtil
									.mkStringFromSubseq(r, in).toUpperCase());

							SafeHtmlBuilder explanation = new SafeHtmlBuilder();

							for (int i = 0; i < in.length(); i++) {
								boolean highlight = false;
								for (int j = 0; j < r.length; j++)
									if (r[j] == i) {
										highlight = true;
										break;
									}
								if (highlight) {
									explanation
											.appendHtmlConstant("<span class=\"highlight\">");
									explanation.appendEscaped(in.substring(i,
											i + 1).toUpperCase());
									explanation.appendHtmlConstant("</span>");
								} else
									explanation.appendEscaped(in.substring(i,
											i + 1));
							}

							mkExplanationPopup(explanation.toSafeHtml(),
									expLabel);
							outputDiv.add(expLabel);
						}

						if (result.acronyms.length == 0)
							showMessage("No acronyms found :( Try a longer title or a shorter acronym length!");

						loadingPanel.hide();
					}
				});
	}

	private void go() {
		if (!input.getText().isEmpty()) {
			loadingPanel.center();
			loadingPanel.show();

			if (modeSelection.getSelectedIndex() == 0)
				generateSubseq();
			else
				generate();
		}
	}

	public void onModuleLoad() {
		input.addStyleName("inputBox");
		input.setMaxLength(120);

		for (int i = AcronymGenerator.MIN_WORD_LENGTH + 1; i <= 7; i++) {
			lengthSelection.addItem(Integer.toString(i) + " ± 1 characters");
		}

		lengthSelection.addItem("8 characters and more");
		lengthSelection.setSelectedIndex(2);

		modeSelection.addItem("Any subsequence");
		modeSelection.addItem("One character per word");
		modeSelection.setSelectedIndex(0);

		allowSkipWords.setText("Allow skipping words");
		allowSkipWords
				.setTitle("If this box is checked, some words may be skipped, otherwise one letter will be picked from each word");

		RootPanel.get("settingsContainer").add(settingsPanel);

		settingsPanel.add(new InlineLabel("Search mode: "));
		settingsPanel.add(modeSelection);

		modeSettingsPanel.add(new InlineLabel("Acronym length: "));
		modeSettingsPanel.add(lengthSelection);

		settingsPanel.add(modeSelection);
		settingsPanel.add(modeSettingsPanel);

		loadingPanel.setWidget(new Image("loaderb64.gif"));
		loadingPanel.setGlassEnabled(true);
		loadingPanel.setModal(true);

		modeSelection.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				modeSettingsPanel.clear();

				if (modeSelection.getSelectedIndex() == 0) {
					modeSettingsPanel.add(new InlineLabel("Acronym length: "));
					modeSettingsPanel.add(lengthSelection);
				} else {
					modeSettingsPanel.add(allowSkipWords);
				}
			}
		});

		goButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				go();
			}
		});

		input.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					go();
			}
		});

		final FlowPanel inputPanel = new FlowPanel();

		inputPanel.add(label);
		inputPanel.add(input);
		inputPanel.add(goButton);

		RootPanel.get("textBoxContainer").add(inputPanel);
	}
}
