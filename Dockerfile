FROM hseeberger/scala-sbt:11.0.2_2.12.8_1.2.8

RUN ["apt-get", "update"]
RUN ["apt-get", "install", "-y", "vim"]

ARG project_dir=/myapp
WORKDIR $project_dir
COPY ./ $project_dir

CMD [ "sbt", "run" ]