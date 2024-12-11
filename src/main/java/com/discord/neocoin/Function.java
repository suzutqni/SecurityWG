package com.discord.neocoin;

import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class Function {	
	
	// テキストチャンネルの削除をスケジュールするメソッド
	protected static void scheduleChannelDeletion(TextChannel channel, long delay, TimeUnit unit) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.schedule(() -> {
			channel.delete().queue(success -> System.out.println("Deleted text channel: " + channel.getName()),
					failure -> System.out.println("Failed to delete text channel: " + failure.getMessage()));
		}, delay, unit);

		// 必要に応じてスケジューラをシャットダウン
		scheduler.shutdown();
	}

	// ボイスチャンネルの削除をスケジュールするメソッド
	protected static void scheduleChannelDeletion(VoiceChannel channel, long delay, TimeUnit unit) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.schedule(() -> {
			channel.delete().queue(success -> System.out.println("Deleted voice channel: " + channel.getName()),
					failure -> System.out.println("Failed to delete voice channel: " + failure.getMessage()));
		}, delay, unit);

		// 必要に応じてスケジューラをシャットダウン
		scheduler.shutdown();
	}

	// 名前からカテゴリを取得するメソッド
	protected static Category getCategoryByName(Guild guild, String name) {
		for (Category category : guild.getCategories()) {
			if (category.getName().equalsIgnoreCase(name)) {
				return category;
			}
		}
		return null;
	}

	public static Role getRoleByName(Guild guild, String roleName) {
		// サーバー内の全てのロールを取得し、指定したロール名に一致するロールを検索
		for (Role role : guild.getRoles()) {
			if (role.getName().equalsIgnoreCase(roleName)) {
				return role;
			}
		}
		return null; // 一致するロールが見つからない場合はnullを返す
	}

	public static void getAllUser(Guild guild) {
		guild.loadMembers().onSuccess(members -> {
			// メンバーがロードされた後に処理を行う
			for (Member member : members) {
				String displayName = member.getEffectiveName();
				System.out.println(displayName);
			}
		}).onError(throwable -> {
			// エラーが発生した場合の処理
			throwable.printStackTrace();
		});
	}
	
	public static String format(String target, int length){
        int byteDiff = (getByteLength(target, Charset.forName("UTF-8"))-target.length())/2;
        return String.format("%-"+(length-byteDiff)+"s", target);
    }

    private static int getByteLength(String string, Charset charset) {
        return string.getBytes(charset).length;
    }

}
