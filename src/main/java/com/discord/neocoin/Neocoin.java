package com.discord.neocoin;

import java.sql.SQLException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Neocoin extends ListenerAdapter {
	@SuppressWarnings("unused")
	private static JDA jda = null;
	private static final String BOT_TOKEN = "";

	public static void main(String[] args) {

		jda = JDABuilder.createDefault(BOT_TOKEN)
				.setRawEventsEnabled(true)
				.enableIntents(GatewayIntent.GUILD_MEMBERS) // GUILD_MEMBERSインテントを有効にする
				.enableIntents(GatewayIntent.MESSAGE_CONTENT)
				.addEventListeners(new Neocoin())
				.setActivity(Activity.playing("neocoin"))
				.build();

	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String[] messageSplit = event.getMessage().getContentRaw().split("\\s+");
		String command = messageSplit[0];
		if (command.equalsIgnoreCase("!コマンド")) {
			try {
				CreateRoom.makeCommandRoom(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("!口座開設")) {
			try {
				AccountController.createAccount(event);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("!取引履歴")) {
			AccountController.transactionHistory(event);
		} else if (command.equalsIgnoreCase("!残高照会")) {
			try {
				if (messageSplit.length == 1) {
					AccountController.inquiryAccount(event);
				} else if (messageSplit[1].equals("AllUsers") && messageSplit[2].equals("pass")) {
					AccountController.inquiryAllAccount(event);
				} else if (messageSplit[1].equals("pass")) {
					AccountController.inquiryIdentifcationAccount(event, messageSplit[2]);
				} else {
					event.getChannel().sendMessage("ERROR：残高照会の形式が間違っています").queue();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("!振込")) {
			try {
				if (messageSplit.length == 3) {
					AccountController.remittance(event, messageSplit[1], messageSplit[2]);
				} else {
					event.getChannel().sendMessage("ERROR：コマンド 送り先 金額の形式で指定してください").queue();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("!ホテル")) {
			try {
				if (messageSplit.length == 3) {
					AccountController.buyHotel(event, messageSplit[1], messageSplit[2]);
				} else if (messageSplit.length == 4) {
					AccountController.buyHotel(event, messageSplit[1], messageSplit[2], messageSplit[3]);
					}
				else {
					event.getChannel().sendMessage("ERROR：[!ホテル 部屋の種類]の形式で指定してください").queue();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("!ロール")) {
			try {
				AccountController.buyRoll(event, messageSplit[1]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (command.equalsIgnoreCase("!給与分配") && messageSplit[1].equals("pass")) {
			AccountController.salary(event);
		} else if (command.equalsIgnoreCase("!名前")){
			Function.getAllUser(event.getGuild());
		}
	}
}
