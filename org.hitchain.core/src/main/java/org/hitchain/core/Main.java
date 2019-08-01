/*******************************************************************************
 * Copyright (c) 2019-03-22 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.core;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Hit;
import org.eclipse.jgit.api.HitConfigCommand;
import org.eclipse.jgit.api.HitRepositoryContractCommand;
import org.eclipse.jgit.api.KeypairCommand;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.hitchain.contract.api.HitRepositoryContractEthereumApi;
import org.hitchain.contract.ethereum.HitRepositoryContractEthereumService;
import org.hitchain.hit.util.HitHelper;
import org.iff.infra.util.MapHelper;
import org.iff.infra.util.NumberHelper;

import java.io.File;
import java.util.*;

/**
 * Main
 * <pre>
 * ==~/.hit/config==
 * [main]
 *     default=encrypt
 * [account]
 *     default=accountName1
 *     accountName1_address=hex
 *     accountName1_private=encrypt
 *     accountName2_address=hex
 *     accountName2_private=encrypt
 * [rsa]
 *     default=rsaName1
 *     rsaName1_pub=hex
 *     rsaName1_pri=encrypt
 *     rsaName2_pub=hex
 *     rsaName2_pri=encrypt
 * [storage]
 *     default=urlName1
 *     urlName1=url
 *     urlName2=url
 * [repository]
 *     default=urlName1
 *     urlName1=url
 *     urlName2=url
 * ==command==
 * hit cfg help
 * hit cfg create password
 * hit cfg [account|rsa] [add|remove] name [priKey] [pubKey] password
 * hit cfg [storage|repository] [add|remove] name url
 * hit cfg [account|rsa|storage|repository] set name
 * </pre>
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-03-22
 * auto generate by qdp.
 */
public class Main {
    public static final String HELP_CFG = "" +
            "hit cfg help\n" +
            "hit cfg create\n" +
            "hit cfg recover    password|account-pri-key|rsa-pri-key\n" +
            "hit cfg account    add name [priKey]\n" +
            "hit cfg rsa        add name [priKey pubKey]\n" +
            "hit cfg storage    add name url\n" +
            "hit cfg repository add name url\n" +
            "hit cfg chain      add name url(https://ropsten.infura.io, https://mainnet.infura.io/0x7995ab36bB307Afa6A683C24a25d90Dc1Ea83566)\n" +
            "hit cfg chainapi   add name url(http://api-ropsten.etherscan.io/api, https://api.etherscan.io/api)\n" +
            "hit cfg gas        add name deployGas deployGwei writeGas writeGwei\n" +
            "hit cfg contract   add name address\n" +
            "hit cfg contract   deploy name\n" +
            "hit cfg [account|rsa|storage|repository|chain|chainapi|gas|contract] remove name\n" +
            "hit cfg [account|rsa|storage|repository|chain|chainapi|gas|contract] set    name";
    public static final String HELP_REPO = "" +
            "hit repository-name help\n" +
            "hit repository-name uri\n";
    public static final String HELP_CONTRACT = "" +
            "hit contract help\n" +
            "hit contract add-repository repositoryName\n" +
            "hit contract user\n" +
            "hit contract email\n" +
            "hit contract max-id\n" +
            "hit contract owner\n" +
            "hit contract id   [repositoryName]\n" +
            "hit contract name [repositoryNameForUpdate]\n" +
            "hit contract url  [repositoryUrlForUpdate]\n" +
            "hit contract contains-[delegator|member|pr-member] address\n" +
            "hit contract count-[delegator|member|pr-member|pr-auth|pr-comm|started]\n" +
            "hit contract index-of-[delegator|member|pr-member|pr-auth|pr-comm|started] index\n" +
            "hit contract is-[delegator|member|pr-member|pr-auth|pr-comm]-disable\n" +
            "hit contract disable-[delegator|member|pr-member|pr-auth|pr-comm]\n" +
            "hit contract update-repository  repositoryName\n" +
            "hit contract update-url         repositoryUrl\n" +
            "hit contract [add|remove]-[delegator|member|pr-member]      address\n" +
            "hit contract add-pr                      prUrl\n" +
            "hit contract add-started                 repositoryUrl\n" +
            "hit contract remove-started              index\n" +
            "hit contract list-[repository|history-url|member|pr-member|pr|pr-auth|pr-comm]\n";
    public static final String HELP_TOKEN = "" +
            "hit token help\n" +
            "hit token eth          [account]\n" +
            "hit token hit          [account]\n" +
            "hit token request-test [account]\n";
    public static final String HELP_ENCRYPT = "" +
            "hit encrypt help\n" +
            "hit encrypt add|renew|remove\n";
    public static final String HELP_AM = "" +
            "hit am help\n" +
            "hit am pathId [--ignore-space-change|--ignore-white-sapce|--force-merge|--no-commit]\n";
    public static final String HELP_MIGRATE = "" +
            "hit migrate help\n" +
            "hit migrate uri\n";

    public static void main0(String[] args) throws Exception {
        //        System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/mergepr/hellopr");
//        System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/mergepr/migrate");
//        System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/migratetest/jfinal");
//        args = new String[]{"pullRequest", "create", "5000000", "10", "-m", "test pull request"};
//        args = new String[]{"pr", "listAuthed"};
//        args = new String[]{"am", "05c6a12b7f6bd6a16ad57cbbefa8fff56cf330c4"};
//        args = new String[]{"pr", "fetch", "https://github.com/ethereum/ethereumj.git"};
//        args = new String[]{"pr", "fetch", "https://gitee.com/jfinal/jfinal.git"};
//        args = new String[]{"migrate", "https://gitee.com/jfinal/jfinal.git"};
//        args = new String[]{"token", "readToken", "0xf49ac47ae8b8ad61a5fa3858224969e07c35f3fa"};
        System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/hellonew");
        args = new String[]{"push"};
        System.out.println("ARG:" + Arrays.toString(args));
        {
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client.protocol.ResponseProcessCookies", "fatal");
        }
        //
        //this just pre-input password.
        {
            Map<String, Set<String>> needPasswords = MapHelper.toMap(
                    "cfg", new HashSet<>(Arrays.asList("account", "rsa", "recover", "contract")),
                    "contract", new HashSet<>(Arrays.asList("add-repository",
                            "disable-delegator", "disable-member", "disable-pr-member", "disable-pr-auth", "disable-pr-comm",
                            "update-repository", "update-url",
                            "add-delegator", "add-member", "add-pr-member", "add-pr", "add-started",
                            "remove-delegator", "remove-member", "remove-pr-member", "remove-started"
                    )),
                    "encrypt", new HashSet<>(),
                    "migrate", new HashSet<>(),
                    "push", new HashSet<>(),
                    "fetch", new HashSet<>()
            );
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            if (!HitHelper.getHitConfig().isEmpty() && args.length > 0) {
                Set<String> strings = needPasswords.get(list.poll());
                if (strings != null && (strings.isEmpty() || strings.contains(list.poll()))) {
                    HitHelper.getAccountPriKeyWithPasswordInput();
                }
            }
        }
        //
        if (args != null && args.length > 0 && "help".equals(args[0])) {
            System.out.println(HELP_CFG);
            System.out.println(HELP_REPO);
            System.out.println(HELP_CONTRACT);
            System.out.println(HELP_TOKEN);
            System.out.println(HELP_ENCRYPT);
            System.out.println(HELP_AM);
            System.out.println(HELP_MIGRATE);
        }
        //
        if (args != null && args.length > 0 && "cfg".equals(args[0])) {
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();
            if (list.isEmpty()) {
                HitHelper.hitConfigInfo(null);
                return;
            }
            String type = list.poll();//account, rsa, storage, repository
            String operation = list.poll();//add, remove, set
            String name = list.poll();
            String p1 = list.poll(), p2 = list.poll(), p3 = list.poll(), p4 = list.poll();// [priKey] [pubKey] password, url, null
            if (HitHelper.TYPE_help.equals(type)) {
                System.out.println(HELP_CFG);
                return;
            }
            if (HitHelper.TYPE_create.equals(type)) {
                Hit.hitConfig().cmd(new HitConfigCommand.CreateCommand()).call();
                return;
            }
            if (HitHelper.TYPE_account.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.AccountCommand().add(name, p1)).call();
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.AccountCommand().remove(name)).call();
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.AccountCommand().set(name)).call();
                    return;
                }
                HitHelper.accountInfo(name);
                return;
            }
            if (HitHelper.TYPE_rsa.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    String pri = StringUtils.isBlank(p2) ? null : p1;
                    String pub = StringUtils.isBlank(p2) ? null : p2;
                    Hit.hitConfig().cmd(new HitConfigCommand.RSACommand().add(name, pri, pub)).call();
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.RSACommand().remove(name)).call();
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.RSACommand().set(name)).call();
                    return;
                }
                HitHelper.rsaInfo(name);
                return;
            }
            if (HitHelper.TYPE_storage.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.StorageCommand().add(name, p1)).call();
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.StorageCommand().remove(name)).call();
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.StorageCommand().set(name)).call();
                    return;
                }
                HitHelper.storageInfo(name);
                return;
            }
            if (HitHelper.TYPE_repository.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.RepositoryCommand().add(name, p1)).call();
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.RepositoryCommand().remove(name)).call();
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.RepositoryCommand().set(name)).call();
                    return;
                }
                HitHelper.repositoryInfo(name);
                return;
            }
            if (HitHelper.TYPE_recover.equals(type)) {
                if ("password".equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.RecoverCommand().password()).call();
                    return;
                }
                if ("account-pri-key".equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.RecoverCommand().accountPrivateKey()).call();
                    return;
                }
                if ("rsa-pri-key".equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.RecoverCommand().rsaPrivateKey()).call();
                    return;
                }
                return;
            }
            if (HitHelper.TYPE_chain.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.ChainCommand().add(name, p1)).call();
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.ChainCommand().remove(name)).call();
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.ChainCommand().set(name)).call();
                    return;
                }
                HitHelper.chainInfo(name);
                return;
            }
            if (HitHelper.TYPE_chainapi.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.ChainApiCommand().add(name, p1)).call();
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.ChainApiCommand().remove(name)).call();
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.ChainApiCommand().set(name)).call();
                    return;
                }
                HitHelper.chainApiInfo(name);
                return;
            }
            if (HitHelper.TYPE_gas.equals(type)) {
                //hit cfg gas        add name deployGas deployGwei writeGas writeGwei
                if (HitHelper.ACTION_add.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.GasCommand().add(name, StringUtils.join(new String[]{p1, p2, p3, p4}, ","))).call();
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.GasCommand().remove(name)).call();
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.GasCommand().set(name)).call();
                    return;
                }
                HitHelper.gasInfo(name);
                return;
            }
            if (HitHelper.TYPE_contract.equals(type)) {
                //hit cfg contract        add name
                if (HitHelper.ACTION_add.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.ContractCommand().add(name, p1)).call();
                    return;
                }
                if (HitHelper.ACTION_deploy.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.ContractCommand().deploy(name)).call();
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.ContractCommand().remove(name)).call();
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    Hit.hitConfig().cmd(new HitConfigCommand.ContractCommand().set(name)).call();
                    return;
                }
                HitHelper.gasInfo(name);
                return;
            }

            System.out.println(HELP_CFG);
            return;
        }
        /*-----------------------------------------------------repository name-----------------------------------------------------*/
        else if (args != null && args.length > 0 && "repository-name".equals(args[0])) {
            if (args.length > 1) {
                System.out.println(Hit.repositoryName().uri(args[1]).call());
                return;
            }
            System.out.println(HELP_REPO);
            return;
        }
        /*-----------------------------------------------------contract new-----------------------------------------------------*/
        else if (args != null && args.length > 0 && "contract".equals(args[0])) {
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();
            if (list.isEmpty()) {
                list.add(HitHelper.TYPE_help);
            }
            String operation = list.poll();// read, write, deploy, help
            String p1 = list.poll(), p2 = list.poll(), p3 = list.poll(), p4 = list.poll();
            if (HitHelper.TYPE_help.equals(operation)) {
                System.out.println(HELP_CONTRACT);
                return;
            }
            if ("add-repository".equals(operation)) {
                File file = tryGetGitDirectory();
                String repositoryName = p1;
                if (StringUtils.isBlank(repositoryName) && file != null) {
                    repositoryName = file.getParentFile().getName();
                }
                if (StringUtils.isNotBlank(repositoryName)) {
                    String result = Hit.createRepository().name(repositoryName).call();
                    if (file != null) {
                        String contract = HitHelper.getContract();
                        HitRepositoryContractEthereumApi api = HitRepositoryContractEthereumService.getApi();
                        int repoId = api.readIdByName(HitHelper.getAccountAddress(), HitHelper.getContract(), repositoryName);
                        String hitUri = "hit://" + contract + "-" + repoId + ".git";

                        StoredConfig config = new FileRepository(file).getConfig();
                        config.load();
                        String uri = config.getString("remote", "origin", "url");
                        // if is blank or is not the right hit uri then change the uri to the right hit uri.
                        if (!(StringUtils.isBlank(uri) || (uri.startsWith("hit://") && !StringUtils.contains(uri, contract)))) {
                            return;
                        }
                        config.setString("remote", "origin", "url", hitUri);
                        config.save();
                        System.out.println("Update remote origin url to " + hitUri);
                    }
                    System.out.println(result);
                } else {
                    System.err.println("Can not find repository.");
                }
                return;
            }
            File projectDir = getGitDirectory();
            Repository repo = new FileRepository(projectDir);
            Hit hit = new Hit(repo);
            HitRepositoryContractCommand cmd = hit.contract();
            if ("user".equals(operation)) {
                if (StringUtils.isBlank(p1)) {
                    System.out.println(cmd.userName());
                } else {
                    System.out.println(cmd.updateUserName(p1));
                }
                return;
            }
            if ("email".equals(operation)) {
                if (StringUtils.isBlank(p1)) {
                    System.out.println(cmd.email());
                } else {
                    System.out.println(cmd.updateEmail(p1));
                }
                return;
            }
            if ("max-id".equals(operation)) {
                System.out.println(cmd.maxId());
                return;
            }
            if ("owner".equals(operation)) {
                System.out.println(cmd.owner());
                return;
            }
            if ("id".equals(operation)) {
                if (StringUtils.isBlank(p1)) {
                    System.out.println(cmd.repositoryId());
                } else {
                    System.out.println(cmd.readId(p1));
                }
                return;
            }
            if ("name".equals(operation)) {
                if (StringUtils.isBlank(p1)) {
                    System.out.println(cmd.repositoryName());
                } else {
                    System.out.println(cmd.updateRepository(p1));
                }
                return;
            }
            if ("url".equals(operation)) {
                if (StringUtils.isBlank(p1)) {
                    System.out.println(cmd.readUrl());
                } else {
                    System.out.println(cmd.updateUrl(p1));
                }
                return;
            }
            if ("contains-delegator".equals(operation)) {
                System.out.println(cmd.hasAddress(HitRepositoryContractEthereumApi.TYPE_DELEGATOR, p1));
                return;
            }
            if ("contains-member".equals(operation)) {
                System.out.println(cmd.hasAddress(HitRepositoryContractEthereumApi.TYPE_MEMBER, p1));
                return;
            }
            if ("contains-pr-member".equals(operation)) {
                System.out.println(cmd.hasAddress(HitRepositoryContractEthereumApi.TYPE_PR_MEMBER, p1));
                return;
            }
            if ("count-delegator".equals(operation)) {
                System.out.println(cmd.readTypeCount(HitRepositoryContractEthereumApi.TYPE_DELEGATOR));
                return;
            }
            if ("count-member".equals(operation)) {
                System.out.println(cmd.readTypeCount(HitRepositoryContractEthereumApi.TYPE_MEMBER));
                return;
            }
            if ("count-pr-member".equals(operation)) {
                System.out.println(cmd.readTypeCount(HitRepositoryContractEthereumApi.TYPE_PR_MEMBER));
                return;
            }
            if ("count-pr-auth".equals(operation)) {
                System.out.println(cmd.readTypeCount(HitRepositoryContractEthereumApi.TYPE_PR_AUTH));
                return;
            }
            if ("count-pr-comm".equals(operation)) {
                System.out.println(cmd.readTypeCount(HitRepositoryContractEthereumApi.TYPE_PR_COMM));
                return;
            }
            if ("count-started".equals(operation)) {
                System.out.println(cmd.readTypeCount(HitRepositoryContractEthereumApi.TYPE_STARTED));
                return;
            }
            if ("index-of-delegator".equals(operation)) {
                System.out.println(cmd.readAddress(HitRepositoryContractEthereumApi.TYPE_DELEGATOR, NumberHelper.getInt(p1, -1)));
                return;
            }
            if ("index-of-member".equals(operation)) {
                System.out.println(cmd.readAddress(HitRepositoryContractEthereumApi.TYPE_MEMBER, NumberHelper.getInt(p1, -1)));
                return;
            }
            if ("index-of-pr-member".equals(operation)) {
                System.out.println(cmd.readAddress(HitRepositoryContractEthereumApi.TYPE_PR_MEMBER, NumberHelper.getInt(p1, -1)));
                return;
            }
            if ("index-of-pr-auth".equals(operation)) {
                System.out.println(cmd.readString(HitRepositoryContractEthereumApi.TYPE_PR_AUTH, NumberHelper.getInt(p1, -1)));
                return;
            }
            if ("index-of-pr-comm".equals(operation)) {
                System.out.println(cmd.readString(HitRepositoryContractEthereumApi.TYPE_PR_COMM, NumberHelper.getInt(p1, -1)));
                return;
            }
            if ("index-of-started".equals(operation)) {
                System.out.println(cmd.readString(HitRepositoryContractEthereumApi.TYPE_STARTED, NumberHelper.getInt(p1, -1)));
                return;
            }
            if ("is-delegator-disable".equals(operation)) {
                System.out.println(cmd.readDisable(HitRepositoryContractEthereumApi.TYPE_DELEGATOR));
                return;
            }
            if ("is-member-disable".equals(operation)) {
                System.out.println(cmd.readDisable(HitRepositoryContractEthereumApi.TYPE_MEMBER));
                return;
            }
            if ("is-pr-member-disable".equals(operation)) {
                System.out.println(cmd.readDisable(HitRepositoryContractEthereumApi.TYPE_PR_MEMBER));
                return;
            }
            if ("is-pr-auth-disable".equals(operation)) {
                System.out.println(cmd.readDisable(HitRepositoryContractEthereumApi.TYPE_PR_AUTH));
                return;
            }
            if ("is-pr-comm-disable".equals(operation)) {
                System.out.println(cmd.readDisable(HitRepositoryContractEthereumApi.TYPE_PR_AUTH));
                return;
            }
            //========================================
            if ("update-repository".equals(operation)) {
                System.out.println(cmd.updateRepository(p1));
                return;
            }
            if ("update-url".equals(operation)) {
                System.out.println(cmd.updateUrl(p1));
                return;
            }
            if ("add-delegator".equals(operation)) {
                System.out.println(cmd.addDelegator(p1));
                return;
            }
            if ("remove-delegator".equals(operation)) {
                System.out.println(cmd.removeDelegator(p1));
                return;
            }
            if ("add-member".equals(operation)) {
                System.out.println(cmd.addMember(p1));
                return;
            }
            if ("remove-member".equals(operation)) {
                System.out.println(cmd.removeMember(p1));
                return;
            }
            if ("add-pr-member".equals(operation)) {
                System.out.println(cmd.addPrMember(p1));
                return;
            }
            if ("remove-pr-member".equals(operation)) {
                System.out.println(cmd.removePrMember(p1));
                return;
            }
            if ("add-pr".equals(operation)) {
                System.out.println(cmd.addPullRequest(p1));
                return;
            }
            if ("add-started".equals(operation)) {
                System.out.println(cmd.addStarted(p1));
                return;
            }
            if ("remove-started".equals(operation)) {
                System.out.println(cmd.removeStarted(NumberHelper.getInt(p1, -1)));
                return;
            }
            if ("disable-delegator".equals(operation)) {
                System.out.println(cmd.disableType(HitRepositoryContractEthereumApi.TYPE_DELEGATOR));
                return;
            }
            if ("disable-member".equals(operation)) {
                System.out.println(cmd.disableType(HitRepositoryContractEthereumApi.TYPE_MEMBER));
                return;
            }
            if ("disable-pr-member".equals(operation)) {
                System.out.println(cmd.disableType(HitRepositoryContractEthereumApi.TYPE_PR_MEMBER));
                return;
            }
            if ("disable-pr-auth".equals(operation)) {
                System.out.println(cmd.disableType(HitRepositoryContractEthereumApi.TYPE_PR_AUTH));
                return;
            }
            if ("disable-pr-comm".equals(operation)) {
                System.out.println(cmd.disableType(HitRepositoryContractEthereumApi.TYPE_PR_COMM));
                return;
            }
            //========================================
            if ("list-repository".equals(operation)) {
                System.out.println(cmd.listRepositories());
                return;
            }
            if ("list-history-url".equals(operation)) {
                System.out.println(cmd.listHistoryUrls());
                return;
            }
            if ("list-member".equals(operation)) {
                System.out.println(cmd.listMembers());
                return;
            }
            if ("list-pr-member".equals(operation)) {
                System.out.println(cmd.listPrMembers());
                return;
            }
            if ("list-pr".equals(operation)) {
                System.out.println(cmd.listPullRequests());
                return;
            }
            if ("list-pr-auth".equals(operation)) {
                System.out.println(cmd.listAuthoredPullRequests());
                return;
            }
            if ("list-pr-comm".equals(operation)) {
                System.out.println(cmd.listCommunityPullRequests());
                return;
            }
            System.out.println(HELP_CONTRACT);
            return;
        }
        /*-----------------------------------------------------token-----------------------------------------------------*/
        else if (args != null && args.length > 0 && "token".equals(args[0])) {
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();
            if (list.isEmpty()) {
                list.add(HitHelper.TYPE_help);
            }
            String type = list.poll();// eth, hit, request-test, help
            String p1 = list.poll();
            File gitDir = tryGetGitDirectory();
            if ("eth".equals(type)) {
                String account = p1;
                if (StringUtils.isBlank(account) && gitDir != null) {
                    account = HitHelper.getAccountAddress();
                }
                if (StringUtils.isNotBlank(account)) {
                    String result = Hit.token().type(type).account(account).call();
                    System.out.println(result);
                    return;
                }
                return;
            }
            if ("hit".equals(type)) {
                String account = p1;
                if (StringUtils.isBlank(account) && gitDir != null) {
                    account = HitHelper.getAccountAddress();
                }
                if (StringUtils.isNotBlank(account)) {
                    String result = Hit.token().type(type).account(account).call();
                    System.out.println(result);
                    return;
                }
                return;
            }
            if ("request-test".equals(type)) {
                String account = p1;
                if (StringUtils.isBlank(account) && gitDir != null) {
                    account = HitHelper.getAccountAddress();
                }
                if (StringUtils.isNotBlank(account)) {
                    String result = Hit.token().type(type).account(account).call();
                    System.out.println(result);
                    return;
                }
                return;
            }
            System.out.println(HELP_TOKEN);
            return;
        }
        /*-----------------------------------------------------encrypt-----------------------------------------------------*/
        else if (args != null && args.length > 0 && "encrypt".equals(args[0])) {
            File gitDir = getGitDirectory();
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();
            if (list.isEmpty()) {
                list.add(HitHelper.TYPE_help);
            }
            String operation = list.poll();// add, remove, renew, help
            Repository repo = new FileRepository(gitDir);
            Hit hit = new Hit(repo, true);
            KeypairCommand keypair = hit.keypair();
            if (HitHelper.ACTION_add.equals(operation)) {
                keypair.add().call();
                return;
            }
            if (HitHelper.ACTION_renew.equals(operation)) {
                keypair.renew().call();
                return;
            }
            if (HitHelper.ACTION_remove.equals(operation)) {
                keypair.remove().call();
                return;
            }
            System.out.println(HELP_ENCRYPT);
            return;
        }
        /*-----------------------------------------------------migrate-----------------------------------------------------*/
        else if (args != null && args.length > 0 && "migrate".equals(args[0])) {
            if (args.length > 1 && StringUtils.isNotBlank(args[1])) {
                Hit.migrate().call();
                return;
            }
            System.out.println(HELP_MIGRATE);
            return;
        }
        /*-----------------------------------------------------am-----------------------------------------------------*/
        else if (args != null && args.length > 0 && "am".equals(args[0])) {
            File gitDir = getGitDirectory();
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();// remove am
            if (list.isEmpty()) {
                list.add(HitHelper.TYPE_help);
            }
            String id = list.poll();// pathId
            String p1 = list.poll(), p2 = list.poll(), p3 = list.poll(), p4 = list.poll();
            if (StringUtils.isBlank(id)) {
                System.out.println(HELP_AM);
                return;
            }

            boolean ignoreSpaceChange = StringUtils.equalsAny("--ignore-space-change", p1, p2, p3, p4);
            boolean ignoreWhitespace = StringUtils.equalsAny("--ignore-white-sapce", p1, p2, p3, p4);
            boolean forceMergeLine = StringUtils.equalsAny("--force-merge", p1, p2, p3, p4);
            boolean noCommit = StringUtils.equalsAny("--no-commit", p1, p2, p3, p4);

            Hit hit = new Hit(new FileRepository(gitDir), true);
            hit.am().patchId(id).ignoreSpaceChange(ignoreSpaceChange).ignoreWhitespace(ignoreWhitespace).forceMergeLine(forceMergeLine).noCommit(noCommit).call();
            IOUtils.closeQuietly(hit);
            return;
        } else {
            Class<?> main = Class.forName("org.eclipse.jgit.pgm.Main");
            main.getMethod(HitHelper.TYPE_main, String[].class).invoke(null, new Object[]{args});
        }
    }

    private static File tryGetGitDirectory() {
        File projectDir = null;
        {
            String workDir = System.getProperty("git_work_tree");
            if (workDir == null) {
                workDir = ".";
            }
            projectDir = new File(workDir + "/.git");
        }
        if (projectDir == null || !projectDir.exists()) {
            return null;
        }
        return projectDir;
    }

    private static File getGitDirectory() {
        File projectDir = tryGetGitDirectory();
        if (projectDir == null || !projectDir.exists()) {
            System.err.println(projectDir.getAbsolutePath() + " is invalid git dir.");
            System.exit(1);
        }
        return projectDir;
    }

    public static void main(String[] args) throws Exception {
        main0(args);
        System.exit(0);
    }
}
