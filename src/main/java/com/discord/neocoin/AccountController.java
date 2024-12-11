package com.discord.neocoin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AccountController extends Function {
	// localhost の 8080 ポート、データベース test に接続
	private static final String URL = "jdbc:postgresql://192.168.0.207:8080/postgres?characterEncoding=UTF-8&useSSL=false";

	public static void inquiryAccount(MessageReceivedEvent event) throws SQLException {
		// DB のユーザ名を user、パスワードを pass に設定
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");
		String SQL = "SELECT \"account_name\",\"account_id\", \"balance\", \"account_updateDate\" FROM neocoin.\"neocoin_Account\" WHERE \"account_id\" = ?";

		try {
			// コネクションを取得して SQL を実行
			Connection con = DriverManager.getConnection(URL, props);
			PreparedStatement ps = con.prepareStatement(SQL);

			String userId = event.getAuthor().getName();
			ps.setString(1, userId);

			//timestampのフォーマットを作成
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			//SELECT文を実行
			ResultSet rs = ps.executeQuery();

			if (rs != null) {
				// 実行結果のデータを表示
				while (rs.next()) {
					event.getChannel().sendMessage("ユーザー名：" + rs.getString("account_name")).queue();
					event.getChannel().sendMessage("ユーザーID：" + rs.getString("account_id")).queue();
					event.getChannel().sendMessage("　　　残高：" + String.format("%,d", rs.getInt("balance"))).queue();
					event.getChannel().sendMessage("最終更新日：" + sdf.format(rs.getTimestamp("account_updateDate")))
							.queue();
				}
			} else {
				event.getChannel().sendMessage("ERROR：口座が存在しません").queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getChannel().sendMessage("ERROR：残高照会に失敗しました").queue();
		}
	}

	public static void inquiryAllAccount(MessageReceivedEvent event) {
		// DB のユーザ名を user、パスワードを pass に設定
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");
		String SQL = "SELECT * FROM neocoin.\"neocoin_Account\"";

		try {
			// コネクションを取得して SQL を実行
			Connection con = DriverManager.getConnection(URL, props);
			PreparedStatement ps = con.prepareStatement(SQL);

			//timestampのフォーマットを作成
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			//SELECT文を実行
			ResultSet rs = ps.executeQuery();

			if (rs != null) {
				// 実行結果のデータを表示
				while (rs.next()) {
					event.getChannel().sendMessage("ユーザー名：" + rs.getString("account_name")).queue();
					event.getChannel().sendMessage("残高：" + String.format("%,d", rs.getInt("balance"))).queue();
					event.getChannel().sendMessage("最終更新日：" + sdf.format(rs.getTimestamp("account_updateDate")))
							.queue();
					event.getChannel().sendMessage("--------------------------------------").queue();
				}
			} else {
				event.getChannel().sendMessage("ERROR：口座が存在しません").queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getChannel().sendMessage("ERROR：残高照会に失敗しました").queue();
		}
	}

	public static void inquiryIdentifcationAccount(MessageReceivedEvent event, String userName) {
		// DB のユーザ名を user、パスワードを pass に設定
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");
		String SQL = "SELECT \"account_name\", \"balance\", \"account_updateDate\" FROM neocoin.\"neocoin_Account\" WHERE \"account_id\" = ?";

		try {
			// コネクションを取得して SQL を実行
			Connection con = DriverManager.getConnection(URL, props);
			PreparedStatement ps = con.prepareStatement(SQL);

			ps.setString(1, userName);

			//timestampのフォーマットを作成
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			//SELECT文を実行
			ResultSet rs = ps.executeQuery();

			if (rs != null) {
				// 実行結果のデータを表示
				while (rs.next()) {
					event.getChannel().sendMessage("ユーザー名：" + rs.getString("account_name")).queue();
					event.getChannel().sendMessage("残高：" + String.format("%,d", rs.getInt("balance"))).queue();
					event.getChannel().sendMessage("最終更新日：" + sdf.format(rs.getTimestamp("account_updateDate")))
							.queue();
				}
			} else {
				event.getChannel().sendMessage("ERROR：口座が存在しません").queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getChannel().sendMessage("ERROR：残高照会に失敗しました").queue();
		}
	}

	public static void createAccount(MessageReceivedEvent event) throws SQLException {
		// DB のユーザ名を user、パスワードを pass に設定
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");
		String CHECK = "SELECT \"account_id\" FROM neocoin.\"neocoin_Account\" WHERE \"account_id\" = ?";
		String CREATE = "INSERT INTO neocoin.\"neocoin_Account\"(\"account_name\", \"account_id\") VALUES(?,?)";
		String LOG = "INSERT INTO neocoin.\"neocoin_Transaction_history\"(userid, transaction_type, \"userName\", \"receive_userName\", amount) VALUES (?,?,?,?,?)";

		try {

			// コネクションを取得して SQL を実行
			Connection con = DriverManager.getConnection(URL, props);
			PreparedStatement psCheck = con.prepareStatement(CHECK);

			PreparedStatement createAcount = con.prepareStatement(CREATE);
			PreparedStatement addLOG = con.prepareStatement(LOG);

			String userName = event.getAuthor().getEffectiveName();
			String userId = event.getAuthor().getName();

			psCheck.setString(1, userId);

			ResultSet rs = psCheck.executeQuery();
			if (!rs.equals(null)) {
				createAcount.setString(1, userName);
				createAcount.setString(2, userId);

				addLOG.setString(1, userId);
				addLOG.setString(2, "口座作成");
				addLOG.setString(3, "Lawless");
				addLOG.setString(4, userName);
				addLOG.setInt(5, 10000);

				// 実行結果のデータを表示
				if (!(createAcount.executeUpdate() == 0) && !(addLOG.executeUpdate() == 0)) {
					event.getChannel().sendMessage("口座作成完了！").queue();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getChannel().sendMessage("ERROR：口座作成に失敗しました").queue();
		}
	}

	public static void remittance(MessageReceivedEvent event, String sendUserID, String strMoney) throws SQLException {
		// DB のユーザ名を user、パスワードを pass に設定
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");
		String CHECK = "SELECT \"account_id\", \"balance\" FROM neocoin.\"neocoin_Account\" WHERE \"account_id\" = ?";
		String RIM = "UPDATE neocoin.\"neocoin_Account\" SET \"balance\" = \"balance\" - ? , \"account_updateDate\" = CURRENT_TIMESTAMP WHERE \"account_id\" = ?";
		String ARR = "UPDATE neocoin.\"neocoin_Account\" SET \"balance\" = \"balance\" + ? , \"account_updateDate\" = CURRENT_TIMESTAMP WHERE \"account_id\" = ?";
		String LOG = "INSERT INTO neocoin.\"neocoin_Transaction_history\"(userid, transaction_type, \"userName\", \"receive_userName\", amount) VALUES (?,?,?,?,?)";
		String LOG2 = "INSERT INTO neocoin.\"neocoin_Transaction_history\"(userid, transaction_type, \"userName\", \"receive_userName\", amount) VALUES (?,?,?,?,?)";

		String userId = event.getAuthor().getName();
		String userName = event.getMember().getEffectiveName();
		int money = Integer.parseInt(strMoney);

		try {

			// コネクションを取得して SQL を実行
			Connection con = DriverManager.getConnection(URL, props);
			PreparedStatement psCheck = con.prepareStatement(CHECK);

			PreparedStatement rimit = con.prepareStatement(RIM);
			PreparedStatement arrival = con.prepareStatement(ARR);

			PreparedStatement addLOG = con.prepareStatement(LOG);
			PreparedStatement addLOG2 = con.prepareStatement(LOG2);

			psCheck.setString(1, userId);
			ResultSet rs = psCheck.executeQuery();

			if (rs.next()) {
				//金額が送金額以上なら実行
				if (money <= rs.getInt("balance")) {
					rimit.setInt(1, money);
					rimit.setString(2, userId);

					arrival.setInt(1, money);
					arrival.setString(2, sendUserID);

					addLOG.setString(1, userId);
					addLOG.setString(2, "送金　　");
					addLOG.setString(3, userName);
					addLOG.setString(4, sendUserID);
					addLOG.setInt(5, money);

					addLOG2.setString(1, sendUserID);
					addLOG2.setString(2, "入金　　");
					addLOG2.setString(3, userName);
					addLOG2.setString(4, sendUserID);
					addLOG2.setInt(5, money);

					if (!(rimit.executeUpdate() == 0) && !(arrival.executeUpdate() == 0)
							&& !(addLOG.executeUpdate() == 0) && !(addLOG2.executeUpdate() == 0)) {
						event.getChannel().sendMessage("送金完了！").queue();
					}
				} else {
					event.getChannel().sendMessage("ERROR:送金額は所持している金額より少ない額を指定してください").queue();
				}
			} else {
				event.getChannel().sendMessage("ERROR:指定したユーザーが存在しません").queue();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			event.getChannel().sendMessage("ERROR：送金に失敗しました").queue();
		}
	}

	public static void buyHotel(MessageReceivedEvent event, String HotelName, String RoomName) throws SQLException {
		// DB のユーザ名を user、パスワードを pass に設定
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");

		String ACCOUNTCHECK = "SELECT \"balance\" FROM neocoin.\"neocoin_Account\" WHERE \"account_id\" = ?";
		String HOTELCHECK = "SELECT \"Hotel_Cost\" FROM neocoin.\"neocoin_BuyHotel_Mst\" WHERE \"Hotel_Name\" = ?";
		String BUY = "UPDATE neocoin.\"neocoin_Account\" SET \"balance\" = \"balance\" - ? , \"account_updateDate\" = CURRENT_TIMESTAMP WHERE \"account_id\" = ?";
		String LOG = "INSERT INTO neocoin.\"neocoin_Transaction_history\"(userid, transaction_type, \"userName\", \"receive_userName\", amount) VALUES (?,?,?,?,?)";

		String userId = event.getAuthor().getName();
		String userName = event.getAuthor().getEffectiveName();
		try {

			// コネクションを取得して SQL を実行
			Connection con = DriverManager.getConnection(URL, props);
			PreparedStatement psCheck = con.prepareStatement(ACCOUNTCHECK);
			PreparedStatement psCheck2 = con.prepareStatement(HOTELCHECK);

			PreparedStatement exeBUY = con.prepareStatement(BUY);
			PreparedStatement addLOG = con.prepareStatement(LOG);

			psCheck.setString(1, userId);
			psCheck2.setString(1, HotelName);
			ResultSet rs = psCheck.executeQuery();
			ResultSet rs2 = psCheck2.executeQuery();

			while (rs.next() && rs2.next()) {

				//口座内の金額が部屋の金額以上なら実行
				int balance = rs.getInt("balance");
				int hotel_cost = rs2.getInt("Hotel_Cost");
				if (hotel_cost <= balance) {

					exeBUY.setInt(1, hotel_cost);
					exeBUY.setString(2, userId);

					addLOG.setString(1, userId);
					addLOG.setString(2, "部屋建て");
					addLOG.setString(3, userName);
					addLOG.setString(4, "Lawless");
					addLOG.setInt(5, hotel_cost);

					if (!(exeBUY.executeUpdate() == 0) && !(addLOG.executeUpdate() == 0)) {
						System.out.println("ok");

						// イベントが発生したギルド（サーバー）を取得
						Guild guild = event.getGuild();

						// ギルドIDを取得
						String guildId = guild.getId();

						// 指定した名前のカテゴリを取得
						Category category = getCategoryByName(guild, "ホテル");

						// 現在の日時を取得
						LocalDateTime now = LocalDateTime.now();

						// 12時間後の日時を計算
						LocalDateTime futureDateTime = now.plusHours(12);

						// フォーマットを指定して表示
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

						// 面接待ちロールを取得
						Role tempRole = getRoleByName(guild, "面接待ち");

						// イベントからユーザーを取得
						Member author = event.getMember();

						// ボイスチャンネルを作成
						guild.createVoiceChannel(RoomName + "：～" + futureDateTime.format(formatter))
								.setUserlimit(2)
								.setParent(category)
								.queue(voiceChannel -> {

									//面接待ちにはボイスチャンネルを表示しない
									voiceChannel.upsertPermissionOverride(tempRole)
											.deny(Permission.VIEW_CHANNEL)
											.queue();

									//フリーダムならチャンネルの権限を与える
									if (HotelName.equals("フリーダム")) {
										voiceChannel.upsertPermissionOverride(author)
												.setAllowed(Permission.ALL_CHANNEL_PERMISSIONS)
												.queue();
									}

									// 12時間後にボイスチャンネルを削除する
									scheduleChannelDeletion(voiceChannel, 12, TimeUnit.HOURS);

									System.out.println("Created voice channel: " + voiceChannel.getName());
								});

						// ギルドIDをコンソールに出力
						System.out.println("Guild ID:" + guildId);

						event.getChannel().sendMessage("部屋建て完了！").queue();
					}
				} else {
					event.getChannel().sendMessage("ERROR:neocoinが足りません").queue();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			event.getChannel().sendMessage("ERROR：お問い合わせください").queue();
		}

	}

	public static void buyHotel(MessageReceivedEvent event, String HotelName, String RoomName, String inviteUserName) {
		// DB のユーザ名を user、パスワードを pass に設定
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");

		String ACCOUNTCHECK = "SELECT \"balance\" FROM neocoin.\"neocoin_Account\" WHERE \"account_id\" = ?";
		String INVITEACCOUNTCHECK = "SELECT \"account_name\", \"account_id\" FROM neocoin.\"neocoin_Account\" WHERE \"account_name\" = ? or \"account_id\" = ?";
		String HOTELCHECK = "SELECT \"Hotel_Cost\" FROM neocoin.\"neocoin_BuyHotel_Mst\" WHERE \"Hotel_Name\" = ?";
		String BUY = "UPDATE neocoin.\"neocoin_Account\" SET \"balance\" = \"balance\" - ? , \"account_updateDate\" = CURRENT_TIMESTAMP WHERE \"account_id\" = ?";
		String LOG = "INSERT INTO neocoin.\"neocoin_Transaction_history\"(userid, transaction_type, \"userName\", \"receive_userName\", amount) VALUES (?,?,?,?,?)";

		String userId = event.getAuthor().getName();
		String userName = event.getAuthor().getEffectiveName();

		try {

			// コネクションを取得して SQL を実行
			Connection con = DriverManager.getConnection(URL, props);

			PreparedStatement psCheck = con.prepareStatement(ACCOUNTCHECK);
			PreparedStatement psCheck2 = con.prepareStatement(HOTELCHECK);
			PreparedStatement psCheck3 = con.prepareStatement(INVITEACCOUNTCHECK);

			PreparedStatement exeBUY = con.prepareStatement(BUY);
			PreparedStatement addLOG = con.prepareStatement(LOG);

			psCheck.setString(1, userId);
			psCheck2.setString(1, HotelName);
			psCheck3.setString(1, inviteUserName);
			psCheck3.setString(2, inviteUserName);

			ResultSet rs = psCheck.executeQuery();
			ResultSet rs2 = psCheck2.executeQuery();
			ResultSet rs3 = psCheck3.executeQuery();

			if (rs.next()) {
				if (rs2.next()) {
					if (rs3.next()) {
						//口座内の金額が部屋の金額以上なら実行
						int balance = rs.getInt("balance");
						int hotel_cost = rs2.getInt("Hotel_Cost");

						if (hotel_cost <= balance) {

							exeBUY.setInt(1, hotel_cost);
							exeBUY.setString(2, userId);

							addLOG.setString(1, userId);
							addLOG.setString(2, "部屋建て");
							addLOG.setString(3, userName);
							addLOG.setString(4, "Lawless");
							addLOG.setInt(5, hotel_cost);

							// イベントが発生したギルド（サーバー）を取得
							Guild guild = event.getGuild();

							if (!(exeBUY.executeUpdate() == 0) && !(addLOG.executeUpdate() == 0)) {
								// 指定した名前のカテゴリを取得
								Category category = getCategoryByName(guild, "ホテル");

								// 現在の日時を取得
								LocalDateTime now = LocalDateTime.now();

								// 12時間後の日時を計算
								LocalDateTime futureDateTime = now.plusHours(12);

								// フォーマットを指定して表示
								DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

								// ギルドIDを取得
								String guildId = guild.getId();

								// @everyone ロールを取得
								Role everyoneRole = guild.getPublicRole();

								// 各ロールを取得
								Role tempRole = getRoleByName(guild, "面接待ち");
								Role tuningRole = getRoleByName(guild, "調律者");
								Role tranceRole = getRoleByName(guild, "超越者");
								Role heroRole = getRoleByName(guild, "覚醒勇者");

								// イベントからユーザーを取得
								Member author = event.getMember();

								// ボイスチャンネルを作成
								guild.createVoiceChannel(RoomName + "：～" + futureDateTime.format(formatter))
										.setUserlimit(2)
										.setParent(category)
										.queue(voiceChannel -> {

											//面接待ちにはボイスチャンネルを表示しない
											voiceChannel.upsertPermissionOverride(tempRole)
													.deny(Permission.VIEW_CHANNEL)
													.queue();

											//招待者以外に接続させない
											voiceChannel.upsertPermissionOverride(everyoneRole)
													.deny(Permission.VOICE_CONNECT)
													.queue();

											//フリーダムならチャンネルの権限を与える
											if (HotelName.equals("シークレット")) {
												voiceChannel.upsertPermissionOverride(everyoneRole)
														.deny(Permission.VIEW_CHANNEL)
														.queue();
												voiceChannel.upsertPermissionOverride(tranceRole)
														.setAllowed(Permission.VIEW_CHANNEL)
														.queue();
												voiceChannel.upsertPermissionOverride(heroRole)
														.setAllowed(Permission.VIEW_CHANNEL)
														.queue();
												voiceChannel.upsertPermissionOverride(tuningRole)
														.setAllowed(Permission.VIEW_CHANNEL)
														.queue();
											}

											//フリーダムならチャンネルの権限を与える
											if (HotelName.equals("フリーダム")) {
												voiceChannel.upsertPermissionOverride(author)
														.setAllowed(Permission.ALL_CHANNEL_PERMISSIONS)
														.queue();
											}

											//部屋主に権限付与
											voiceChannel.upsertPermissionOverride(author)
													.setAllowed(Permission.VIEW_CHANNEL)
													.queue();
											voiceChannel.upsertPermissionOverride(author)
													.setAllowed(Permission.VOICE_CONNECT)
													.queue();

											//招待されたユーザーに権限付与
											guild.loadMembers().onSuccess(members -> {
												Search: for (Member member : members) {
													if (member.getUser().getName().equals(inviteUserName)
															|| member.getUser().getEffectiveName()
																	.equals(inviteUserName)) {
														voiceChannel.upsertPermissionOverride(member)
																.setAllowed(Permission.VIEW_CHANNEL)
																.queue();
														voiceChannel.upsertPermissionOverride(member)
																.setAllowed(Permission.VOICE_CONNECT)
																.queue();
														break Search;
													}
												}
											}).onError(throwable -> {
												// エラーが発生した場合の処理
												throwable.printStackTrace();
											});

											// 12時間後にボイスチャンネルを削除する
											scheduleChannelDeletion(voiceChannel, 12, TimeUnit.HOURS);

											System.out.println("Created voice channel: " + voiceChannel.getName());
										});

								// ギルドIDをコンソールに出力
								System.out.println("Guild ID:" + guildId);

								event.getChannel().sendMessage("部屋建て完了！").queue();
							}
						} else {
							event.getChannel().sendMessage("ERROR:neocoinが足りません").queue();
						}

					} else {
						event.getChannel().sendMessage("ERROR:指定したユーザーが存在しません").queue();
					}
				} else {
					event.getChannel().sendMessage("ERROR:指定したホテル名が存在しません").queue();
				}
			} else {
				event.getChannel().sendMessage("ERROR:口座情報が存在しません").queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getChannel().sendMessage("ERROR：お問い合わせください").queue();
		}

	}

	public static void buyRoll(MessageReceivedEvent event, String rollname) throws SQLException {
		// DB のユーザ名を user、パスワードを pass に設定
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");
		String ACCOUNTCHECK = "SELECT \"balance\" FROM neocoin.\"neocoin_Account\" WHERE \"account_id\" = ?";
		String ROLLCHECK = "SELECT roll_cost FROM neocoin.\"neocoin_Roll_Mst\" WHERE roll_name = ?";
		String BUY = "UPDATE neocoin.\"neocoin_Account\" SET \"balance\" = \"balance\" - ? , \"account_updateDate\" = CURRENT_TIMESTAMP WHERE \"account_id\" = ?";
		String LOG = "INSERT INTO neocoin.\"neocoin_Transaction_history\"(userid, transaction_type, \"userName\", \"receive_userName\", amount) VALUES (?,?,?,?,?)";

		String userId = event.getAuthor().getName();
		String userName = event.getAuthor().getEffectiveName();

		try {

			// コネクションを取得して SQL を実行
			Connection con = DriverManager.getConnection(URL, props);
			PreparedStatement psCheck = con.prepareStatement(ACCOUNTCHECK);
			PreparedStatement psCheck2 = con.prepareStatement(ROLLCHECK);
			PreparedStatement exeBUY = con.prepareStatement(BUY);
			PreparedStatement addLOG = con.prepareStatement(LOG);

			psCheck.setString(1, userId);
			psCheck2.setString(1, rollname);
			ResultSet rs = psCheck.executeQuery();
			ResultSet rs2 = psCheck2.executeQuery();

			if (!rs.equals(null) && !rs2.equals(null)) {
				while (rs.next() && rs2.next()) {
					//口座内の金額が部屋の金額以上なら実行
					int balance = rs.getInt("balance");
					int roll_cost = rs2.getInt("roll_cost");
					if (roll_cost == 0) {
						event.getChannel().sendMessage("ERROR:このロールは購入できません").queue();
					} else if (roll_cost <= balance) {
						exeBUY.setInt(1, roll_cost);
						exeBUY.setString(2, userId);

						addLOG.setString(1, userId);
						addLOG.setString(2, "購入");
						addLOG.setString(3, userName);
						addLOG.setString(4, "Lawless");
						addLOG.setInt(5, roll_cost);
						if (!(exeBUY.executeUpdate() == 0) && !(addLOG.executeUpdate() == 0)) {

							// イベントが発生したギルド（サーバー）を取得
							Guild guild = event.getGuild();

							//購入するロールを取得
							Role role = getRoleByName(guild, rollname);

							User author = event.getAuthor();

							//メンバーにロールを付与
							guild.addRoleToMember(author, role).queue();

							event.getChannel().sendMessage("ロール付与完了！").queue();

						}
					} else {
						event.getChannel().sendMessage("ERROR:neocoinが足りません").queue();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getChannel().sendMessage("ERROR：お問い合わせください").queue();
		}
	}

	public static void transactionHistory(MessageReceivedEvent event) {

		// DB のユーザ名を user、パスワードを pass に設定
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");
		String SQL = "SELECT transaction_type , \"userName\" , \"receive_userName\" , amount , register_date FROM neocoin.\"neocoin_Transaction_history\" WHERE \"userid\" = ?";

		try {
			// コネクションを取得して SQL を実行
			Connection con = DriverManager.getConnection(URL, props);
			PreparedStatement ps = con.prepareStatement(SQL);

			String userId = event.getAuthor().getName();
			ps.setString(1, userId);

			//timestampのフォーマットを作成
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			//SELECT文を実行
			ResultSet rs = ps.executeQuery();

			if (rs != null) {
				// 実行結果のデータを表示
				event.getChannel().sendMessage("取引種別　　受取者　　入金額　　取引日").queue();
				event.getChannel().sendMessage("-----------------------------------------------").queue();
				int i = 0;
				while (rs.next()) {
					event.getChannel().sendMessage(format(rs.getString("transaction_type"), 18) + ""
							+ rs.getString("receive_userName") + "　　"
							+ String.format("%,d", rs.getInt("amount")) + "　　"
							+ sdf.format(rs.getTimestamp("register_date"))).queue();
					if (i == 10) {
						event.getChannel().sendMessage("10件以上のログが取得したい場合は銀行従業員にご連絡ください").queue();
						break;
					} else {
						i++;
					}
				}
			} else {
				event.getChannel().sendMessage("ERROR：取引履歴が存在しません").queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getChannel().sendMessage("ERROR：取引履歴照会に失敗しました").queue();
		}
	}

	public static void salary(MessageReceivedEvent event) {
		// DB のユーザ名を user、パスワードを pass に設定
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");
		String ACCOUNT = "SELECT \"account_id\", \"balance\" FROM neocoin.\"neocoin_Account\"";
		String SALARY = "UPDATE neocoin.\"neocoin_Account\" SET \"balance\" = \"balance\" + ? , \"account_updateDate\" = CURRENT_TIMESTAMP WHERE \"account_id\" = ?";
		String ROLL = "SELECT roll_name , roll_salary FROM neocoin.\"neocoin_Roll_Mst\"";
		String LOG = "INSERT INTO neocoin.\"neocoin_Transaction_history\"(userid, transaction_type, \"userName\", \"receive_userName\", amount) VALUES (?,?,?,?,?)";

		// イベントが発生したギルド（サーバー）を取得
		Guild guild = event.getGuild();
		try {

			// コネクションを取得して SQL を実行
			Connection con = DriverManager.getConnection(URL, props);

			PreparedStatement accountCheck = con.prepareStatement(ACCOUNT);
			PreparedStatement rollCheck = con.prepareStatement(ROLL);
			PreparedStatement addSalary = con.prepareStatement(SALARY);
			PreparedStatement addLOG = con.prepareStatement(LOG);

			ResultSet rs = accountCheck.executeQuery();
			ResultSet rs2 = rollCheck.executeQuery();

			guild.loadMembers().onSuccess(members -> {
				try {
					while (rs.next()) {
						// メンバーがロードされた後に処理を行う
						for (Member member : members) {

							String userId = member.getUser().getName();
							String userName = member.getUser().getEffectiveName();
							List<Role> roles = member.getRoles();

							int salary = 0;
							while (rs2.next()) {
								String roll_name = rs2.getString("roll_name");
								int roll_salary = rs2.getInt("roll_salary");
								for (Role role : roles) {
									if (role.getName().equals(roll_name)) {
										salary += roll_salary;

									}
								}
							}
							addSalary.setInt(1, salary);
							addSalary.setString(2, userId);
							addLOG.setString(1, userId);
							addLOG.setString(2, "給与");
							addLOG.setString(3, "Lawless");
							addLOG.setString(4, userName);
							addLOG.setInt(5, salary);
							if (addSalary.executeUpdate() == 0 || addLOG.executeUpdate() == 0)
								event.getChannel()
										.sendMessage("ERROR：" + userName + "(" + userId + ")" + "の給与分配に失敗しました").queue();
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}).onError(throwable -> {
				// エラーが発生した場合の処理
				throwable.printStackTrace();
			});

			event.getChannel().sendMessage("給与分配完了！").queue();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void changeEffectiveName(MessageReceivedEvent event, String changeName) {
		Member member = event.getMember();

		if (member != null) {
			member.modifyNickname("新しい表示名").queue(
					success -> event.getChannel().sendMessage("表示名の変更が完了しました").queue(),
					error -> event.getChannel().sendMessage("表示名の変更に失敗しました").queue());
		}
	}
}