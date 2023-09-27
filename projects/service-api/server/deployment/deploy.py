#! /usr/bin/env python

import argparse
import datetime
import pystache
import subprocess

# Example of commands this deployment script will execute
# gcloud auth activate-service-account --key-file /tmp/creds.json
# docker build -t gcr.io/masterypath-staging/web:v1 .
# gcloud --project=masterypath-staging docker -- push gcr.io/masterypath-staging/web:v1
# gcloud config set container/use_v1_api_client false
# gcloud beta container clusters get-credentials cluster-web --project masterypath-staging --zone us-central1-a
# kubectl apply -f k8-staging.yml
# kubectl rollout status deployment/masterypath -w

def setup_argument_parser():
    parser = argparse.ArgumentParser()
    parser.add_argument('--gcloud-auth-file',
                        help='The json credential file for the gcloud service account',
                        required=True)
    parser.add_argument('--gcloud-project', help='The gcloud project name',
                        required=True)
    parser.add_argument('--assembly-point',
                        help='Where the complete system has been assembled',
                        required=True)
    parser.add_argument('--prod-or-staging',
                        help='Whether this is a prod or staging system',
                        required=True)
    return parser

def get_version():
    hash_of_repo = subprocess.check_output(["git", "log", "--pretty=format:%h",
                                            "-n", "1"])

    commit_count = subprocess.check_output(["git",
                                            "rev-list",
                                            "HEAD",
                                            "--count"]).decode("UTF-8").strip()

    dirty = subprocess.Popen("git diff-index --quiet HEAD --", shell=True).wait()
    timestamp = datetime.datetime.now().strftime("%Y-%m-%dT%H.%M.%S")
    release = ".%s-%s" % (hash_of_repo.decode("utf-8"), timestamp)

    if dirty:
        release = ".dirty-" + timestamp

    return commit_count + release

def push_container(assembly_point, gcloud_auth_file, gcloud_project, remote_repo):
    subprocess.check_call(["docker", "build", "-t", remote_repo, assembly_point])
    subprocess.check_call(["gcloud", "auth", "activate-service-account", "--key-file", gcloud_auth_file])
    subprocess.check_call(["gcloud", "--project=" + gcloud_project, "docker", "--", "push", remote_repo])

def cloud_run_deploy(service_name, image_name, gcloud_project):
    subprocess.check_call(["gcloud", "run", "deploy", service_name, "--project", gcloud_project, "--platform", "managed", "--region", "us-central1", "--image", image_name])

# def apply_kubernetes(remote_repo, gcloud_project, gcloud_database_instance, hosts):
#     variables = {'GCR_REPO': remote_repo,
#                  'GCLOUD_DATABASE_INSTANCE': gcloud_database_instance,
#                  'HOST_NAMES': hosts}
#     yaml_contents = ""
#     with open('./deployment/k8s-staging.yml', 'r') as deployment_template:
#         yaml_contents = pystache.render(deployment_template.read(), variables)
#
#     yaml_name = "./.deployment.yaml"
#     with open(yaml_name, "w") as resolved_template:
#         resolved_template.write(yaml_contents)
#         print "wrote deployment description:\n" + yaml_contents
#
#     subprocess.check_call(["gcloud",
#                            "config",
#                            "set", "container/use_v1_api_client", "false"])
#
#     subprocess.check_call(["gcloud", "beta", "container",
#                            "clusters", "get-credentials", "cluster-staging",
#                            "--project", gcloud_project, "--zone", "us-central1-f"])
#     subprocess.check_call(["kubectl", "apply", "-f", yaml_name])
#     subprocess.check_call(["kubectl", "rollout", "status", "deployment/masterypath", "-w"])

def deploy(args):
    assembly_point = args.assembly_point
    version = get_version()
    image_name = "masterypath-" + args.prod_or_staging
    remote_repo = "us.gcr.io/" + args.gcloud_project + "/" + image_name + ":" + version
    push_container(assembly_point, args.gcloud_auth_file, args.gcloud_project, remote_repo)
    cloud_run_deploy(args.prod_or_staging, remote_repo, args.gcloud_project)

def main():
    parser = setup_argument_parser()
    args = parser.parse_args()
    deploy(args)

if __name__ == "__main__":
    main()