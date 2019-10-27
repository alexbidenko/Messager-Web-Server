window.onload = function() {
    M.textareaAutoResize($('#text-message'));
};

var ws = new WebSocket("ws://127.0.0.1:8080/chat");

ws.onopen = function () {};

ws.onclose = function (event) {
    if(event.wasClean) {
    } else {
    }
};

ws.onmessage = function (event) {
    var data = JSON.parse(event.data);
    if(!!data.you) {
        ChatApp.me = data.you;
    } else if (!!data.new) {
        if(!ChatApp.all_chats[data.new]) {
            ChatApp.$set(ChatApp.all_chats, data.new, {
                messages: [],
                is_online: true,
                new_mess: 0
            });
        } else {
            ChatApp.$set(ChatApp.all_chats[data.new], 'is_online', true);
        }
    } else if (!!data.ofline) {
        ChatApp.$set(ChatApp.all_chats[data.ofline], 'is_online', false);
    } else if (!!data.finish_load) {
        ChatApp.finish_load = data.finish_load;
    } else if (data.message != "") {
        let chat = (data.to == 'to_all' ? data.to : (data.from == ChatApp.me ? data.to : data.from));
        ChatApp.all_chats[chat].messages.push({owner: (data.from == ChatApp.me ? "me" : data.from), message: data.message});
        ChatApp.$set(ChatApp.all_chats[chat], 'new_mess', ChatApp.all_chats[chat].new_mess + 1);
    } else if (data.image != "") {
        let chat = (data.to == 'to_all' ? data.to : (data.from == ChatApp.me ? data.to : data.from));
        ChatApp.all_chats[chat].messages.push({owner: (data.from == ChatApp.me ? "me" : data.from), image: data.image});
        ChatApp.$set(ChatApp.all_chats[chat], 'new_mess', ChatApp.all_chats[chat].new_mess + 1);
    }
};

ws.onerror = function (error) {
};

$('#send-mes').click(function () {

});

function AddImage(src) {
    setTimeout(function () {
        let ans = {
            to_who: ChatApp.speaker,
            image: src
        };
        ws.send(JSON.stringify(ans));
        if (ans.to_who != "to_all")
            ChatApp.all_chats[ans.to_who].messages.push({owner: "me", image: ans.image});
    }, 1000);
}

function SendPhoto (el) {
    var files = $(el).get(0).files;
    for (let i = 0; i < files.length; i++) {
        var formData = new FormData();

        formData.append('files', files[i]);

        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/post_file", true);
        xhr.send(formData);

        xhr.onreadystatechange = function() {
            if (xhr.readyState != 4) return;

            if (xhr.status != 200) {
                alert(xhr.status + ': ' + xhr.statusText);
            } else {
                AddImage(xhr.responseText);
            }
        }
    }
}

var ChatApp = new Vue({
    el: '#chat-app',
    data: {
        finish_load: false,
        me: "",
        speaker: "",
        all_chats: {
            to_all: {
                messages: [],
                is_online: true,
                new_mess: 0
            }
        },
        all_messages: []
    },
    methods: {
        SendMessage: function () {
            let ans = {
                to_who: this.speaker,
                message: $('textarea').val()
            };
            ws.send(JSON.stringify(ans));
            if (ans.to_who != "to_all")
                this.all_chats[ans.to_who].messages.push({owner: this.me, message: ans.message});
            $('textarea').val("");
        },
        ChangeChat: function(el, user) {
            ChatApp.speaker = user;
            ChatApp.all_messages = ChatApp.all_chats[ChatApp.speaker].messages;
            $(el).children('span').remove();
            if(ChatApp.speaker == 'to_all') {
                $('.alert-dark').eq(1).text("Сообщения всех пользователей:");
            } else {
                $('.alert-dark').eq(1).text("Сообщения с пользователем: " + ChatApp.speaker);
            }
            this.$set(this.all_chats[user], 'new_mess', 0);
        }
    },
    created() {
        var formData = new FormData();

        formData.append('user', this.me);

        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/get-messages", true);
        xhr.send(formData);

        xhr.onreadystatechange = function() {
            if (xhr.readyState != 4) return;

            if (xhr.status != 200) {
                alert(xhr.status + ': ' + xhr.statusText);
            } else {
                let ans = JSON.parse(xhr.responseText);
                for (let i in ans) {
                    let chat = (ans[i].to == 'to_all' ? ans[i].to : (ans[i].from == ChatApp.me ? ans[i].to : ans[i].from));
                    if(!ChatApp.all_chats[chat]) {
                        ChatApp.$set(ChatApp.all_chats, chat, {
                            messages: [],
                            is_online: false,
                            new_mess: 0
                        });
                    }
                    ChatApp.all_chats[chat].messages.push({
                        owner: ans[i].from,
                        message: ans[i].message
                    });
                }
            }
        }
    },
    computed: {
        count_chats: function () {
            let count = 0;
            for (let i in this.all_chats) {
                count++;
            }
            return count;
        }
    }
});