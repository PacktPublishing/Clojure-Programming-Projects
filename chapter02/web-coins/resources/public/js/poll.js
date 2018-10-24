<script type="text/javascript" charset="utf-8">
    function addmsg(type, msg){
        $("#messages").append(
            "<div class='msg "+ type +"'>"+ msg +"</div>"
        );
    }
    function waitForMsg(){
        $.ajax({
            type: "GET",
            url: "news.php",
            async: true,
            cache: false,
            timeout:50000,

            success: function(data){
                addmsg("new", data);
                setTimeout(
                    waitForMsg,
                    1000
                );
            },
            error: function(XMLHttpRequest, textStatus, errorThrown){
                addmsg("error", textStatus + " (" + errorThrown + ")");
                setTimeout(
                    waitForMsg,
                    15000);
            }
        });
    };
    $(document).ready(function(){
        waitForMsg();
    });
</script>
