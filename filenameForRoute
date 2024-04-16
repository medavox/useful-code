# creates a filename for a complex URL, eg a REST endpoint
# eg https://foo.bar.baz.qux.example.co.uk/a/b/c/d/dosomething becomes example.co.uk-dosomething
# eg https://foo.bar.example.com/a/b/c/d/dosomething becomes bar.example.com-dosomething
# TODO: support query parameters
# TODO: support URLs with <3 dotted parts (eg https://example.com)
# TODO: support other (and absent) protocols
function filenameForRoute() {
    input=$1
    noprotocol="${input#https://}"
    domain=${noprotocol%%/*}
    prearray=${domain//./ }
    read -r -a array <<< "$prearray"
    end=$(basename $input)
    echo ${array[-3]}.${array[-2]}.${array[-1]}-$end
}
