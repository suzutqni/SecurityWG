package com.discord.neocoin;

import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CreateRoom extends Function{

	protected static void makeCommandRoom(MessageReceivedEvent event) {
		// コマンドを打ったユーザーの情報
		Member author = event.getMember();
		String UserId = author.getUser().getName();

		// イベントが発生したギルド（サーバー）を取得
		Guild guild = event.getGuild();

		// 指定した名前のカテゴリを取得
		Category category = getCategoryByName(guild, "ニコニコ銀行コマンドルーム");

		// @everyone ロールを取得
		Role everyoneRole = guild.getPublicRole();

		// チャンネルの作成
		guild.createTextChannel(UserId + ":コマンドルーム")
		.setParent(category)
		.queue(commandChannel -> {
				
				// ボットとコマンドを打ったユーザー以外には閲覧権限を与えない
				commandChannel.upsertPermissionOverride(everyoneRole)
				.setDenied(Permission.ALL_CHANNEL_PERMISSIONS)
				.queue();

				// コマンドを打ったユーザーに対して閲覧権限を付与
				commandChannel.upsertPermissionOverride(author)
				.setAllowed(Permission.ALL_CHANNEL_PERMISSIONS)
				.queue();
				// 60秒後にボイスチャンネルを削除する
				scheduleChannelDeletion(commandChannel, 60, TimeUnit.SECONDS);

				System.out.println("Created command channel: " + commandChannel.getName());
		});

		event.getChannel().sendMessage("コマンドルーム作成完了").queue();
	}
}
