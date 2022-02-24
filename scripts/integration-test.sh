SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )


function check_status() {
    local myvar=$1
    local suffix=$2
    
    if [[ -n ${myvar} ]]; then
        echo "PASSED "${suffix}
    else
        echo "FAILED "${suffix}
    fi
}

function check_reverse_status() {
    local myvar=$1
    local suffix=$2
    
    if [[ -z ${myvar} ]]; then
        echo "PASSED "${suffix}
    else
        echo "FAILED "${suffix}
    fi
}

(
    cd ${SCRIPT_DIR}/..
    mvn package -DskipITs
) &> /tmp/mop-unit-tests.txt

mop_test_status=$(grep "BUILD SUCCESS" /tmp/mop-unit-tests.txt)
check_status "${mop_test_status}" " on mop's unit tests"


(
    cd ~/projects/emop/scripts
    bash make-agent-new.sh props/ agents quiet
    mvn install:install-file -Dfile=agents/JavaMOPAgent.jar -DgroupId="javamop-agent" -DartifactId="javamop-agent" -Dversion="1.0" -Dpackaging="jar"
) &> /tmp/agent-outcome.txt

agent_status=$(grep "JavaMOPAgent.jar is generated." /tmp/agent-outcome.txt)
check_status "${agent_status}" " on agent generation"

install_status=$(grep "BUILD SUCCESS" /tmp/agent-outcome.txt)
check_status "${install_status}" " on agent installation"

error_status=$(grep "error" /tmp/agent-outcome.txt)
echo ${error_status}

(
    if [ ! -d /tmp/commons-fileupload ]; then
        git clone https://github.com/apache/commons-fileupload /tmp/commons-fileupload
    fi
    cd /tmp/commons-fileupload
    git clean -ffxd
    git checkout -f 55dc6fe4d7
    bash ~/projects/emop/scripts/mop-pom-modify/modify-project.sh `pwd` javamop
    export RVMLOGGINGLEVEL=UNIQUE
    mvn test
) &> /tmp/fileupload-outcome.txt

fileupload_status=$(grep "BUILD SUCCESS" /tmp/fileupload-outcome.txt)
check_status "${fileupload_status}" " on fileupload tests"

violation_diffs=$(diff <(sort /tmp/commons-fileupload/violation-counts) <(sort ${SCRIPT_DIR}/commons-fileupload-violation-counts))
check_reverse_status "${violation_diffs}" "on fileupload violations"
