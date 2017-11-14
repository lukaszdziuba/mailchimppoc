package com.averagedev.mailchimppoc;

import com.ecwid.maleorang.MailchimpClient;
import com.ecwid.maleorang.MailchimpException;
import com.ecwid.maleorang.method.v3_0.lists.GetListMethod;
import com.ecwid.maleorang.method.v3_0.lists.ListInfo;
import com.ecwid.maleorang.method.v3_0.members.DeleteMemberMethod;
import com.ecwid.maleorang.method.v3_0.members.EditMemberMethod;
import com.ecwid.maleorang.method.v3_0.members.GetMembersMethod;
import com.ecwid.maleorang.method.v3_0.members.MemberInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MailchimpPOC {



    private static final String KEY_FILE = "/tmp/mailchimppoc.conf";

    /**
     * Gets access token from the file
     *
     * @return
     * @throws IOException
     */
    private static String getApiKey() throws IOException {
        return Files.readAllLines(Paths.get(KEY_FILE)).get(0);
    }

    private static void displayListName(MailchimpClient client, String listId) throws IOException, MailchimpException {
        GetListMethod getList = new GetListMethod(listId);
        ListInfo listInfo = client.execute(getList);
        System.out.println(listInfo.name);
    }

    private static List<MemberInfo> getMembers(MailchimpClient client, String listId) throws IOException, MailchimpException {
        GetMembersMethod getMembers = new GetMembersMethod(listId);
        GetMembersMethod.Response membersInfo = client.execute(getMembers);
        return membersInfo.members;
    }

    private static void displayListMembers(MailchimpClient client, String listId) throws IOException, MailchimpException {
        List<MemberInfo> members = getMembers(client, listId);
        for(MemberInfo member : members) {
            System.out.println(member.email_address);
        }
    }

    private static void displayList(MailchimpClient client, String listId) throws IOException, MailchimpException {
        displayListName(client, listId);
        displayListMembers(client, listId);
    }

    private static void moveFirstSubscriber(MailchimpClient client, String sourceListId, String destListId) throws IOException, MailchimpException {
        List<MemberInfo> members = getMembers(client, sourceListId);
        if(members.isEmpty()) {
            System.out.println("Source list is empty");
        } else {
            MemberInfo firstMember = members.get(0);
            String memberEmail = firstMember.email_address;
            // add the member
            EditMemberMethod editMethod = new EditMemberMethod.CreateOrUpdate(destListId, memberEmail);
            editMethod.merge_fields = firstMember.merge_fields;
            editMethod.status = "subscribed";
            client.execute(editMethod);
            System.out.println("Added " + memberEmail + " to dest list");
            // delete from source list
            DeleteMemberMethod deleteMemberMethod = new DeleteMemberMethod(sourceListId, memberEmail);
            client.execute(deleteMemberMethod);
            System.out.println("Added " + memberEmail + " from source list");
        }
    }

    public static void main(String[] args) throws IOException, MailchimpException {
        // freebie1
        String listId1 = "4a6b0a935f";
        // newsletter
        String listId2 = "35b12275e7";

        MailchimpClient client = new MailchimpClient(getApiKey());
        try {
            //displayList(client, listId1);
            //displayList(client, listId2);
            moveFirstSubscriber(client, listId1, listId2);
        } finally {
            client.close();
        }
    }


}
