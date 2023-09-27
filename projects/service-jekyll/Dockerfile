FROM jekyll/jekyll:4.2.0

ARG VERSION

RUN [[ $VERSION > 0 ]] || exit 1

RUN apk add --no-cache curl

ADD ./themes /home/jekyll/
RUN chown -R jekyll /home/jekyll/

WORKDIR /home/jekyll/memoirs/

RUN jekyll build

ADD ./src/ /app/
RUN chmod 0755 /app/*.sh
RUN chmod 0755 /app/*.py

EXPOSE 8080