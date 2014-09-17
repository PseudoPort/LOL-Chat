package com.tesfayeabel.lolchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.listeners.FriendListener;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;
import com.tesfayeabel.lolchat.R;
import com.tesfayeabel.lolchat.StaticFriend;
import com.tesfayeabel.lolchat.ui.adapter.ExpandableFriendViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainFragment extends LOLChatFragment {
    private ExpandableListView listView;
    //private List<Friend> updateList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lolchat_main, container, false);
        listView = (ExpandableListView) view.findViewById(R.id.listView);
        //updateList = new ArrayList<Friend>();
        if (savedInstanceState != null) {
            listView.onRestoreInstanceState(savedInstanceState.getParcelable("listView"));
        }
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                ExpandableFriendViewAdapter adapter = (ExpandableFriendViewAdapter) listView.getExpandableListAdapter();
                intent.putExtra("player", adapter.getChild(groupPosition, childPosition).getName());
                startActivity(intent);
                return true;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final ExpandableFriendViewAdapter adapter = (ExpandableFriendViewAdapter) listView.getExpandableListAdapter();
//        for (Friend f : updateList) {
//            adapter.setFriendOnline(new StaticFriend(f), f.isOnline());
//        }
    }

    public void onChatConnected(final LolChat chat) {
        ArrayList<StaticFriend> online = new ArrayList<StaticFriend>();
        ArrayList<StaticFriend> offline = new ArrayList<StaticFriend>();
        for (Friend friend : chat.getOnlineFriends()) {
            online.add(new StaticFriend(friend));
        }
        for (Friend friend : chat.getOfflineFriends()) {
            offline.add(new StaticFriend(friend));
        }
        Comparator<StaticFriend> comparator = new Comparator<StaticFriend>() {
            @Override
            public int compare(StaticFriend friend, StaticFriend friend2) {
                return friend.getName().toLowerCase().compareTo(friend2.getName().toLowerCase());
            }
        };
        Collections.sort(online, comparator);
        Collections.sort(offline, comparator);
        listView.setAdapter(new ExpandableFriendViewAdapter(getActivity(), online, offline));
        listView.expandGroup(0);
        chat.addFriendListener(new FriendListener() {

            private void update(final Friend friend) {
                final ExpandableFriendViewAdapter adapter = (ExpandableFriendViewAdapter) listView.getExpandableListAdapter();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.updateFriendStatus(new StaticFriend(friend));
                        }
                    });
                }
            }

            @Override
            public void onFriendAvailable(final Friend friend) {
                update(friend);
            }

            @Override
            public void onFriendAway(final Friend friend) {
                update(friend);
            }

            @Override
            public void onFriendBusy(final Friend friend) {
                update(friend);
            }

            @Override
            public void onFriendJoin(final Friend friend) {
                final ExpandableFriendViewAdapter adapter = (ExpandableFriendViewAdapter) listView.getExpandableListAdapter();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setFriendOnline(new StaticFriend(friend), true);
                        }
                    });
                } else {
                    //updateList.add(friend);
                }
            }

            @Override
            public void onFriendLeave(final Friend friend) {
                final ExpandableFriendViewAdapter adapter = (ExpandableFriendViewAdapter) listView.getExpandableListAdapter();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setFriendOnline(new StaticFriend(friend), false);
                        }
                    });
                } else {
                    //updateList.add(friend);
                }
            }

            @Override
            public void onFriendStatusChange(final Friend friend) {
                update(friend);
            }

            @Override
            public void onNewFriend(Friend friend) {

            }

            @Override
            public void onRemoveFriend(String userId) {

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("listView", listView.onSaveInstanceState());
    }
}
