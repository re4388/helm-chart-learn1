# setup mongo express
```shell
helm repo add cowboysysop https://cowboysysop.github.io/charts/
helm repo update

helm install mongo-express cowboysysop/mongo-express \
--namespace demo-app \
--set service.type=NodePort \
--set mongodbEnableAdmin=true \
--set mongodbAdminUsername="admin" \
--set mongodbAdminPassword="admin" \
--set "extraEnvVars[0].name=ME_CONFIG_MONGODB_URL,extraEnvVars[0].value=mongodb://demo-app-mongodb:27017"



# get svc name to use port-forward

# kubectl get services -n demo-app mongo-express

# NAME            TYPE       CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE                                                                                             │
# mongo-express   NodePort   10.43.89.103   <none>        8081:32580/TCP   30s


# it means it's at 8081

# port-forward to access mongo-express
kubectl port-forward -n demo-app svc/mongo-express 8084:8081

# access mongo-express
http://localhost:8084


# if error, can uninstall and try other options
helm uninstall mongo-express -n demo-app

```

# go inside mongodb to debug
```sh
kubectl exec -it xxxxxxxxxxxxxxxxxxx -n demo-app -- bash 

go to /bin
./mongosh

use demo
show collections
db.posts.find()

```

